package com.hollandjake.dogbot.module.core;

import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.DatabaseModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Boot extends DatabaseModule {
    private boolean silent;
    private boolean moduleOutput;

    @Autowired
    public Boot(MessageService messageService,
                JdbcTemplate template,
                Environment environment) {
        super(messageService, template);
        this.silent = Boolean.parseBoolean(environment.getProperty("boot.silent"));
        this.moduleOutput = Boolean.parseBoolean(environment.getProperty("module.output"));
    }

    public void sendMessage() {
        if (moduleOutput && !silent) {
            messageService.sendMessageWithImage(getRandomResponse(), getRandomImage());
        }
    }
}
