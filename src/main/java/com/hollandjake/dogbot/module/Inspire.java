package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandModule;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;
import static com.hollandjake.dogbot.util.CONSTANTS.GET_PAGE_SOURCE;

public class Inspire extends CommandModule {
    private static final String INSPIRE_REGEX = ACTIONIFY("inspire");

    public Inspire(MessageService messageService) {
        super(messageService);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(INSPIRE_REGEX)) {
                        String imgURL = GET_PAGE_SOURCE("http://inspirobot.me/api?generate=true");
                        messageService.sendMessageWithImage("Inspiring.", imgURL);
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
            if (text.matches(INSPIRE_REGEX)) {
                return INSPIRE_REGEX;
            }
        }
        return "";
    }
}
