package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandModule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Think extends CommandModule {
    private static final String THINK_REGEX = ACTIONIFY("think");
    private static final String MULTI_THINK_REGEX = ACTIONIFY("think (\\d*)");
    private static final String THONK_REGEX = ACTIONIFY("thonk");
    private static final String MULTI_THONK_REGEX = ACTIONIFY("thonk (\\d*)");

    public Think(MessageService messageService) {
        super(messageService);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws MalformedCommandException {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(THINK_REGEX) || match.equals(THONK_REGEX)) {
                        messageService.sendMessage("\uD83E\uDD14");
                    } else if (match.equals(MULTI_THINK_REGEX) || match.equals(MULTI_THONK_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(match).matcher(text);
                        if (matcher.find()) {
                            int repeats = Integer.parseInt(matcher.group(1));
                            if (repeats > 100) {
                                messageService.sendMessage("That's a bit too much thinking right there!");
                            } else {
                                messageService.sendMessage(new String(new char[repeats]).replace("\0", "\uD83E\uDD14"));
                            }
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
            if (text.matches(THINK_REGEX)) {
                return THINK_REGEX;
            } else if (text.matches(MULTI_THINK_REGEX)) {
                return MULTI_THINK_REGEX;
            } else if (text.matches(THONK_REGEX)) {
                return THONK_REGEX;
            } else if (text.matches(MULTI_THONK_REGEX)) {
                return MULTI_THONK_REGEX;
            }
        }
        return "";
    }
}
