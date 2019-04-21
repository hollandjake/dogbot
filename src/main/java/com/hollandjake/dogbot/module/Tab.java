package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandModule;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Tab extends CommandModule {
    private static final String TAB_REGEX = ACTIONIFY("tab");

    public Tab(MessageService messageService) {
        super(messageService);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(TAB_REGEX)) {
                        messageService.sendMessageWithImage("\uD83D\uDEA8 WEE WOO WEE WOO \uD83D\uDEA8",
                                "https://www.hollandjake.com/dogbot/tabulance.png");
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
            if (text.matches(TAB_REGEX)) {
                return TAB_REGEX;
            }
        }
        return "";
    }
}
