package com.hollandjake.dogbot.model;

import com.hollandjake.dogbot.util.Clipbot;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.datatransfer.StringSelection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.hollandjake.dogbot.util.XPATHS.MESSAGE_TEXT;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Text extends MessageComponent {
    @NonNull
    private String data;

    @Builder
    public Text(Integer id, String data) {
        super(id);
        this.data = data;
    }

    public static Text fromString(String text) {
        return Text.builder().data(text).build();
    }

    public static Set<MessageComponent> extractFrom(WebElement messageElement) {
        Set<MessageComponent> messageComponents = new HashSet<>();
        List<WebElement> textComponents = messageElement.findElements(By.xpath(MESSAGE_TEXT));
        for (WebElement textComponent : textComponents) {
            messageComponents.add(Text.builder().data(textComponent.getAttribute("aria-label")).build());
        }
        return messageComponents;
    }

    @Override
    public String prettyPrint() {
        return "\"" + data + "\"";
    }

    @Override
    public void send(WebElement inputBox, WebDriverWait wait, Clipbot clipbot) {
        clipbot.paste(new StringSelection(unescapeHtml(data)), inputBox);
    }
}
