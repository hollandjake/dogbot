package com.hollandjake.dogbot.util.module;

import com.hollandjake.dogbot.service.MessageService;

public abstract class CommandModule extends Module implements CommandableModule {
    public CommandModule(MessageService messageService) {
        super(messageService);
    }
}
