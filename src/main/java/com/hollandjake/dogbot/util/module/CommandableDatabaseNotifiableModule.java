package com.hollandjake.dogbot.util.module;

import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.service.NotificationService;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class CommandableDatabaseNotifiableModule extends CommandableDatabaseModule {

    protected NotificationService notificationService;

    public CommandableDatabaseNotifiableModule(
            MessageService messageService,
            NotificationService notificationService,
            JdbcTemplate template) {
        super(messageService, template);
        this.notificationService = notificationService;
    }
}
