package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class ExtraGoodDogs extends CommandableDatabaseModule {
    private static final String EXTRA_GOOD_DOG_REGEX = ACTIONIFY("extragooddog");
    private static final String EDG_REGEX = ACTIONIFY("egd");

    public ExtraGoodDogs(
            MessageService messageService,
            JdbcTemplate template) {
        super(messageService, template);
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(EXTRA_GOOD_DOG_REGEX) || match.equals(EDG_REGEX)) {
                        messageService.sendMessageWithImage("Extra good woof!", getRandomImage());
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
            if (text.matches(EXTRA_GOOD_DOG_REGEX)) {
                return EXTRA_GOOD_DOG_REGEX;
            } else if (text.matches(EDG_REGEX)) {
                return EDG_REGEX;
            }
        }
        return "";
    }
}
