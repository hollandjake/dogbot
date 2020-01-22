package com.hollandjake.dogbot.controller;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.module.*;
import com.hollandjake.dogbot.module.core.*;
import com.hollandjake.dogbot.module.quote.Grab;
import com.hollandjake.dogbot.module.quote.Quotes;
import com.hollandjake.dogbot.module.reddit.Birds;
import com.hollandjake.dogbot.module.reddit.Cats;
import com.hollandjake.dogbot.module.reddit.Dogs;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandableModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

@Slf4j
@Service
public class ModuleController {
    private final MessageService messageService;
    @Getter
    private final HashMap<String, CommandableModule> modules = new HashMap<>();

    private final JdbcTemplate template;
    private final Boot boot;

    @Value("${image.max-size:1MB}")
    public DataSize maxImageBytes;

    @Value("${module.output:true}")
    public Boolean moduleOutput;

    public ModuleController(MessageService messageService,
                            JdbcTemplate template,
                            Boot boot) {
        this.messageService = messageService;
        this.template = template;
        this.boot = boot;
    }

    public void processMessage(Message message, Boolean freshMessage) {
        try {
            for (CommandableModule module : modules.values()) {
                if (module.process(message, freshMessage, moduleOutput)) {
                    break;
                }
            }
        } catch (MalformedCommandException e) {
            messageService.sendMessage("There seems to be an issue with your command");
        } catch (Exception e) {
            log.error("Something has gone wrong", e);
            messageService.sendMessage("I'm sorry, somethings gone wrong");
        }
    }

    public boolean containsCommand(Message message) {
        return modules.values().stream().anyMatch(
                commandableModule -> message.getMessageComponents().stream().noneMatch(
                        component -> commandableModule.getMatch(component).isEmpty()
                )
        );
    }

    public void onLoad() {
        //Core modules
        boot.sendMessage();
        modules.put("Stats", new Stats(messageService, template));
        modules.put("Reddit", new Reddit(messageService, this));
        modules.put("Ping", new Ping(messageService));

        //Other modules
        modules.put("Commands", new OneLinkCommand(messageService,
                Arrays.asList("commands", "help"),
                "A list of commands can be found at",
                "https://github.com/hollandjake/dogbot", false));
        modules.put("Github", new OneLinkCommand(messageService,
                Arrays.asList("github", "repo", "git"),
                "Github repository",
                "https://github.com/hollandjake/dogbot", false));

        //Modules
        modules.put("Birds", new Birds(messageService, template));
        modules.put("Cats", new Cats(messageService, template));
        modules.put("Dirk", new Dirk(messageService));
        modules.put("Dogs", new Dogs(messageService, template));
        modules.put("EightBall", new EightBall(messageService, template));
        modules.put("ExtraGoodCats", new ExtraGoodCats(messageService));
        modules.put("ExtraGoodDogs", new ExtraGoodDogs(messageService, template));
        modules.put("Gran", new Gran(messageService, template));
        modules.put("Inspire", new Inspire(messageService));
        modules.put("Quotes", new Quotes(messageService, template));
        modules.put("Grab", new Grab(messageService, template, this));
        modules.put("Reacts", new Reacts(messageService, template));
        modules.put("Roll", new Roll(messageService));
        modules.put("Tab", new Tab(messageService));
        modules.put("Think", new Think(messageService));
        modules.put("XKCD", new XKCD(messageService));

        //Extra commands
        modules.put("Feedback", new OneLinkCommand(
                messageService,
                Collections.singletonList("feedback"),
                "Feedback form",
                "https://docs.google.com/document/d/19Vquu0fh8LCqUXH0wwpm9H9MSq1LrEx1Z2Xg9NknKmg/edit?usp=sharing",
                false
        ));
        modules.put("Trello", new OneLinkCommand(
                messageService,
                Collections.singletonList("trello"),
                "Trello",
                "https://trello.com/invite/b/9f49WSW0/5aeb3b9523a722573df5121d65bdad56/second-year-compsci",
                false
        ));
    }
}
