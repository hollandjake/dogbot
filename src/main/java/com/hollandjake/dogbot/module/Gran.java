package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Gran extends CommandableDatabaseModule {
    private static final String GRAN_REGEX = ACTIONIFY("gran");

    public Gran(
            MessageService messageService,
            JdbcTemplate template) {
        super(messageService, template);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws MalformedCommandException {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(GRAN_REGEX)) {
                        messageService.sendMessage("Granny says: \"" + getRandomResponse().getData() + "\"");
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
            if (text.matches(GRAN_REGEX)) {
                return GRAN_REGEX;
            }
        }
        return "";
    }
}
