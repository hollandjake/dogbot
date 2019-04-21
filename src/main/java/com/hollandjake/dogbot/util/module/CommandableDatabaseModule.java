package com.hollandjake.dogbot.util.module;

import com.hollandjake.dogbot.service.MessageService;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class CommandableDatabaseModule extends DatabaseModule implements CommandableModule {
    public CommandableDatabaseModule(
            MessageService messageService,
            JdbcTemplate template) {
        super(messageService, template);
    }
}
