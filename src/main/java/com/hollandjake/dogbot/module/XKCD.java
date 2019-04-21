package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandModule;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.*;

public class XKCD extends CommandModule {
    private static final String XKCD_REGEX = ACTIONIFY("xkcd");
    private static final String LATEST_SHORT_XKCD_REGEX = ACTIONIFY("xkcd l");
    private static final String LATEST_XKCD_REGEX = ACTIONIFY("xkcd latest");
    private static final String SPECIFIC_XKCD_REGEX = ACTIONIFY("xkcd ([1-9][0-9]*)");

    private int highestNumber;

    public XKCD(MessageService messageService) {
        super(messageService);
        this.highestNumber = (int) new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/info.0.json")).get("num");
    }

    private void sendXKCD(int number) {
        this.highestNumber = (int) new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/info.0.json")).get("num");
        if (number < 1 || highestNumber < number) {
            messageService.sendMessage("XKCD number out of range. Please try XKCD's in range 1-" + highestNumber);
        } else {
            JSONObject xkcd = new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/" + number + "/info.0.json"));

            String title = xkcd.get("safe_title").toString();
            String alt = xkcd.get("alt").toString();
            String imgURL = xkcd.get("img").toString();

            String response =
                    "Title: " + title +
                            "\nNumber: " + number +
                            "\nAlt text: " + alt;

            messageService.sendMessageWithImage(response, imgURL);
        }
    }

    private void sendRandomXKCD() {
        sendXKCD(RANDOM.nextInt(highestNumber) + 1);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws MalformedCommandException {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(XKCD_REGEX)) {
                        sendRandomXKCD();
                    } else if (match.equals(LATEST_SHORT_XKCD_REGEX) || match.equals(LATEST_XKCD_REGEX)) {
                        sendXKCD(highestNumber);
                    } else if (match.equals(SPECIFIC_XKCD_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(SPECIFIC_XKCD_REGEX).matcher(text);
                        if (matcher.find()) {
                            int number = Integer.parseInt(matcher.group(1));
                            sendXKCD(number);
                        } else {
                            throw new MalformedCommandException();
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            if (text.matches(XKCD_REGEX)) {
                return XKCD_REGEX;
            } else if (text.matches(LATEST_XKCD_REGEX)) {
                return LATEST_XKCD_REGEX;
            } else if (text.matches(LATEST_SHORT_XKCD_REGEX)) {
                return LATEST_SHORT_XKCD_REGEX;
            } else if (text.matches(SPECIFIC_XKCD_REGEX)) {
                return SPECIFIC_XKCD_REGEX;
            }
        }
        return "";
    }
}
