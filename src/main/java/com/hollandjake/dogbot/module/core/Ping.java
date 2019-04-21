package com.hollandjake.dogbot.module.core;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.CommandModule;
import lombok.Builder;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Ping extends CommandModule {
    private final String PING_REGEX = ACTIONIFY("ping");

    @Builder
    public Ping(MessageService messageService) {
        super(messageService);
    }

    @Override
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            if (text.matches(PING_REGEX)) {
                return PING_REGEX;
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
                    if (match.equals(PING_REGEX)) {
                        if (Math.random() < 0.3) {
                            messageService.sendMessageWithImage(
                                    "Pong! \uD83C\uDFD3",
                                    "https://www.rightthisminute.com/sites/default/files/styles/twitter_card/public/videos/images/munchkin-teddy-bear-dog-ping-pong-video.jpg?itok=ajJWbxY6"
                            );
                        } else {
                            messageService.sendMessage("Pong! \uD83C\uDFD3");
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}