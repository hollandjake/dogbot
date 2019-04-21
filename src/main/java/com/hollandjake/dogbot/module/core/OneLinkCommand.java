package com.hollandjake.dogbot.module.core;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.CONSTANTS;
import com.hollandjake.dogbot.util.module.CommandModule;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class OneLinkCommand extends CommandModule {
    @NonNull
    private final List<String> commandRegexes;
    @NonNull
    private final String url;
    @NonNull
    private final String message;
    @NonNull
    private final boolean outputStaleMessages;

    @Builder
    public OneLinkCommand(
            MessageService messageService,
            List<String> commands,
            String message,
            String url,
            boolean outputStaleMessages) {
        super(messageService);
        this.commandRegexes = commands.stream().map(CONSTANTS::ACTIONIFY).collect(Collectors.toList());
        this.url = url;
        this.message = message;
        this.outputStaleMessages = outputStaleMessages;
    }

    @Override
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            for (String command : commandRegexes) {
                if (text.matches(command)) {
                    return command;
                }
            }
        }
        return "";
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && (outputStaleMessages || freshMessage)) {
                    for (String command : commandRegexes) {
                        if (match.equals(command)) {
                            messageService.sendMessage(this.message + (url.isEmpty() ? "" : (":\n" + url)));
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}