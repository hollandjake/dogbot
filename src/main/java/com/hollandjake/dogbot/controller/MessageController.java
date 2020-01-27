package com.hollandjake.dogbot.controller;

import com.hollandjake.dogbot.ChatbotApplication;
import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.BrowserStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.unit.DataSize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Controller
public class MessageController {
    private final WebController webController;
    @Getter
    private final MessageService messageService;
    @Getter
    private final ChatbotApplication application;
    private final ModuleController moduleController;
    @Value("${image.max-size:1MB}")
    public DataSize maxImageBytes;
    @Value("${messenger.max-messages:10000}")
    private long maxMessagesOnScreen;
    @Getter
    private long numProcessedMessages = 0;

    @Autowired
    public MessageController(
            WebController webController,
            MessageService messageService,
            ChatbotApplication application,
            ModuleController moduleController) {
        java.lang.Thread.setDefaultUncaughtExceptionHandler((thread, e) -> application.errorHandler(this, e));
        this.webController = webController;
        this.messageService = messageService;
        this.application = application;
        this.moduleController = moduleController;
        moduleController.onLoad();
    }

    @Scheduled(fixedDelay = 1)
    public void checkForMessages() {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> application.errorHandler(this, e));
        log.debug("Checking for messages");
        try {
            while (webController.getStatus().equals(BrowserStatus.AWAITING_MESSAGE)) {
                Message latestStoredMessage = messageService.getLatestMessage();

                //Load messages from before it was loaded
                do {
                    Stream<LocalDateTime> dates = messageService.getMessageDates();

                    if (latestStoredMessage != null) {
                        final LocalDateTime timestamp = latestStoredMessage.getTimestamp();
                        if (dates.anyMatch(date -> date.isBefore(timestamp))) {
                            break;
                        }
                    }
                }
                while (!webController.isStartOfChat());

                int numMessagesOnScreen = webController.getNumMessages();
                List<Message> messages = messageService.getUnsavedMessages(latestStoredMessage);
                webController.setStatus(BrowserStatus.PROCESSING_MESSAGE);
                processMessages(messages);
                webController.setStatus(BrowserStatus.AWAITING_MESSAGE);

                if (numMessagesOnScreen > maxMessagesOnScreen) {
                    webController.refresh(false);
                } else {
                    webController.waitForMessagesToChange(numMessagesOnScreen);
                }
            }
        } catch (WebDriverException e) {
            log.info("Another thread has changed the system", e);
        }
    }

    private void processMessages(List<Message> newMessages) {
        if (!newMessages.isEmpty()) {
            newMessages.forEach(message -> {
                webController.jiggle(message.getWebElement());
                webController.highlightElement(message.getWebElement());
                Message savedMessage = messageService.save(message);
                moduleController.processMessage(savedMessage, isFresh(savedMessage));
                numProcessedMessages++;
            });
        }
    }

    private boolean isFresh(Message message) {
        return message.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(30));
    }
}
