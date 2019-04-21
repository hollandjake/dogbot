package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class EightBall extends CommandableDatabaseModule {
    private static final String NO_QUESTION_REGEX = ACTIONIFY("(8ball|ask)");
    private static final String QUESTION_REGEX = ACTIONIFY("(8ball|ask) (.*)");

    public EightBall(
            MessageService messageService,
            JdbcTemplate template) {
        super(messageService, template);
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws MalformedCommandException {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(NO_QUESTION_REGEX)) {
                        messageService.sendMessage("Please enter a question after the command");
                    } else if (match.equals(QUESTION_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(QUESTION_REGEX).matcher(text);
                        if (matcher.find() && !matcher.group(2).isEmpty()) {
                            messageService.sendMessage(getRandomResponse());
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
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            if (text.matches(NO_QUESTION_REGEX)) {
                return NO_QUESTION_REGEX;
            } else if (text.matches(QUESTION_REGEX)) {
                return QUESTION_REGEX;
            }
        }
        return "";
    }
}
