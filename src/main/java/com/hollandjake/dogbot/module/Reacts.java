package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Reacts extends CommandableDatabaseModule {
    private static final String REACT_REGEX = ACTIONIFY("react( (.*))?");
    private static final String REAC_REGEX = ACTIONIFY("reac+( (.*))?");

    public Reacts(
            MessageService messageService,
            JdbcTemplate template) {
        super(messageService, template);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(REACT_REGEX) || match.equals(REAC_REGEX)) {
                        Message sending = Message.fromString(messageService.getThread(),
                                messageService.getMe(),
                                "Judging. \uD83E\uDD14");
                        sending.getMessageComponents().add(getRandomImage());
                        messageService.sendMessage(sending);
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
            if (text.matches(REACT_REGEX)) {
                return REACT_REGEX;
            } else if (text.matches(REAC_REGEX)) {
                return REAC_REGEX;
            }
        }
        return "";
    }
}
