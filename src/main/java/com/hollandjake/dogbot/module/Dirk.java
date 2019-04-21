package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandModule;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Dirk extends CommandModule {
    private static final String DIRK_REGEX = ACTIONIFY("(dirk|god)");

    public Dirk(MessageService messageService) {
        super(messageService);
    }

    @Override
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            if (text.matches(DIRK_REGEX)) {
                return DIRK_REGEX;
            }
        }
        return "";
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(DIRK_REGEX)) {
                        messageService.sendMessageWithImage("Our lord and saviour has greeted us",
                                "https://staffwww.dcs.shef.ac.uk/people/D.Sudholt/Dirk_Sudholt-cropped.jpg");
                    }
                }
                return true;
            }
        }
        return false;
    }
}
