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
import static com.hollandjake.dogbot.util.CONSTANTS.RANDOM;

public class Roll extends CommandModule {
    private static final String ROLL_DICE_REGEX = ACTIONIFY("roll");
    private static final String ROLL_REGEX = ACTIONIFY("roll (\\d+)");

    public Roll(MessageService messageService) {
        super(messageService);
    }

    private void roll(int lower, int upper) {
        int number = RANDOM.nextInt(upper - lower) + lower;
        messageService.sendMessage("You rolled " + number);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws MalformedCommandException {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(ROLL_DICE_REGEX)) {
                        roll(1, 6);
                    } else if (match.equals(ROLL_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(ROLL_REGEX).matcher(text);
                        if (matcher.find() && !matcher.group(1).isEmpty()) {
                            try {
                                roll(1, Integer.parseInt(matcher.group(1)));
                            } catch (NumberFormatException e) {
                                throw new MalformedCommandException();
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
            if (text.matches(ROLL_DICE_REGEX)) {
                return ROLL_DICE_REGEX;
            } else if (text.matches(ROLL_REGEX)) {
                return ROLL_REGEX;
            }
        }
        return "";
    }
}
