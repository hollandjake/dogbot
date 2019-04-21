package com.hollandjake.dogbot.model;

import com.hollandjake.dogbot.util.Clipbot;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.mariadb.jdbc.MariaDbBlob;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.XPATHS.MESSAGE_IMAGE;
import static java.awt.datatransfer.DataFlavor.imageFlavor;

@Data
@Slf4j
public class Image extends MessageComponent implements Transferable {
    private static final Pattern REGEX = Pattern.compile("url\\(\"(\\S+?)\"\\)");

    @NonNull
    private BufferedImage data;

    public Image(Integer id, BufferedImage data) {
        super(id);
        this.data = data;
    }

    @Builder
    public Image(Integer id, Blob blob, long maxBytes) throws SQLException {
        super(id);
        BufferedImage image = imageFromStream(blob.getBinaryStream(), maxBytes);
        if (image != null) {
            this.data = image;
        } else {
            throw new SQLException("Image failed to load");
        }
    }

    private static BufferedImage imageFromUrl(String url, long maxBytes) throws SSLHandshakeException {
        if (url == null || url.isEmpty()) {
            return null;
        }

        BufferedImage image = null;
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.connect();

            image = imageFromStream(urlConnection.getInputStream(), maxBytes);
        } catch (SSLHandshakeException e) {
            throw e;
        } catch (IOException e) {
            log.error("imageFromUrl", e);
        }
        return image;
    }

    private static BufferedImage imageFromStream(InputStream inputStream, long maxBytes) {
        if (inputStream == null) {
            return null;
        }
        ImageInputStream imageInputStream = null;
        BufferedImage image = null;

        try {
            imageInputStream = ImageIO.createImageInputStream(inputStream);
            image = ImageIO.read(imageInputStream);

            if (image != null) {
                int bytesPerPixel = 3; // 3 is for the BufferedImage.TYPE_3BYTE_BGR
                ByteArrayOutputStream out = toByteArrayOutputStream(image);
                int size = out.size();
                double pixelRatio = bytesPerPixel / (image.getColorModel().getPixelSize() / 8.0);
                double scaleFactor = maxBytes / (size * pixelRatio);
                if (scaleFactor < 1 && scaleFactor > 0) {
                    scaleFactor = Math.sqrt(scaleFactor);
                    int scaledWidth = (int) (image.getWidth() * scaleFactor);
                    scaleFactor = (double) scaledWidth / image.getWidth();
                    int scaledHeight = (int) (image.getHeight() * scaleFactor);

                    if (scaledWidth > 0 && scaledHeight > 0) {
                        java.awt.Image scaledImage = image.getScaledInstance(scaledWidth,
                                scaledHeight,
                                java.awt.Image.SCALE_SMOOTH);
                        BufferedImage bufferedImage = new BufferedImage(scaledWidth,
                                scaledHeight,
                                BufferedImage.TYPE_3BYTE_BGR);
                        Graphics2D g = bufferedImage.createGraphics();
                        g.drawImage(scaledImage, 0, 0, null);
                        g.dispose();
                        image = bufferedImage;
                    }
                }
            }
        } catch (IOException e) {
            log.error("imageFromStream", e);
        } finally {
            try {
                if (imageInputStream != null) {
                    imageInputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
        return image;
    }

    public static MessageComponent fromUrl(String url, long maxBytes) {
        try {
            BufferedImage image = imageFromUrl(url, maxBytes);
            return Image.fromBufferedImage(image, maxBytes);
        } catch (IOException e) {
            return Text.fromString(url);
        }
    }

    private static ByteArrayOutputStream toByteArrayOutputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        out.close();
        return out;
    }

    public static Image fromBufferedImage(BufferedImage image, long maxBytes) throws IOException {
        return fromInputStream(toStream(image), maxBytes);
    }

    public static Image fromInputStream(InputStream stream, long maxBytes) {
        BufferedImage image = imageFromStream(stream, maxBytes);
        if (image != null) {
            return new Image(null, image);
        }
        return null;
    }

    public static byte[] toByteArray(BufferedImage image) throws IOException {
        return toByteArrayOutputStream(image).toByteArray();
    }

    public static InputStream toStream(BufferedImage image) throws IOException {
        return toStream(toByteArray(image));
    }

    public static InputStream toStream(byte[] image) {
        return new ByteArrayInputStream(image);
    }

    public static List<MessageComponent> extractFrom(WebElement messageElement, long maxBytes) {
        List<MessageComponent> messageComponents = new ArrayList<>();
        List<WebElement> imageComponents = messageElement.findElements(By.xpath(MESSAGE_IMAGE));
        for (WebElement imageComponent : imageComponents) {
            String style = imageComponent.getAttribute("style");
            Matcher matcher = REGEX.matcher(style);
            if (matcher.find()) {
                String imageUrl = matcher.group(1);
                messageComponents.add(fromUrl(imageUrl, maxBytes));
            }
        }
        return messageComponents;
    }

    public InputStream toStream() {
        try {
            return toStream(data);
        } catch (IOException e) {
            log.error("Failed to make stream", e);
        }
        return null;
    }

    @Override
    public String prettyPrint() {
        return "(\"" + getId() + "\")";
    }

    @Override
    public void send(WebElement inputBox, WebDriverWait wait, Clipbot clipbot) {
        clipbot.paste(this, inputBox);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{imageFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return imageFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return data;
    }

    public BufferedImage toBufferedImage(byte[] imageData, long maxBytes) {
        return toBufferedImage(toStream(imageData), maxBytes);
    }

    public BufferedImage toBufferedImage(InputStream imageData, long maxBytes) {
        return imageFromStream(imageData, maxBytes);
    }

    public Blob getBlob() {
        try {
            return new MariaDbBlob(toByteArray(data));
        } catch (IOException e) {
            log.error("Failed to convert image", e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Image image = (Image) o;
        try {
            byte[] myBlob = Image.toByteArray(this.data);
            byte[] theirBlob = Image.toByteArray(image.data);
            return Arrays.equals(myBlob, theirBlob);
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data);
    }
}
