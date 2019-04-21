package com.hollandjake.dogbot.module.reddit;

import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.module.RedditModule;
import lombok.Builder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Dogs extends RedditModule {
    @Builder
    public Dogs(
            MessageService messageService,
            JdbcTemplate template) {
        super(messageService, template, Arrays.asList(
                ACTIONIFY("dog"),
                ACTIONIFY("doggo")
        ));
    }
}
