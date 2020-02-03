package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.ChatbotApplication;
import com.hollandjake.dogbot.controller.WebController;
import com.hollandjake.dogbot.model.Thread;
import com.hollandjake.dogbot.model.*;
import com.hollandjake.dogbot.repository.MessageRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.StaleElementReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@CacheConfig(cacheNames = "messages")
public class MessageService {
    private final WebController webController;
    @Getter
    private final MessageRepository messageRepository;
    @Getter
    private final ThreadService threadService;
    @Getter
    private final HumanService humanService;
    @Getter
    private final MessageComponentService messageComponentService;
    @Getter
    private final TextService textService;
    @Getter
    private final ImageService imageService;
    @Getter
    private final ChatbotApplication application;
    @Value("${image.max-size:1MB}")
    public DataSize maxImageBytes;

    @Autowired
    public MessageService(
            WebController webController,
            MessageRepository messageRepository,
            ThreadService threadService,
            HumanService humanService,
            MessageComponentService messageComponentService,
            TextService textService,
            ImageService imageService,
            ChatbotApplication application) {
        this.webController = webController;
        this.messageRepository = messageRepository;
        this.threadService = threadService;
        this.humanService = humanService;
        this.messageComponentService = messageComponentService;
        this.textService = textService;
        this.imageService = imageService;
        this.application = application;
    }

    public List<Message> getUnsavedMessages(Message latestStoredMessage) {
        Thread thread = getThread();
        LocalDateTime latestStoredTime = latestStoredMessage != null ? latestStoredMessage.getTimestamp() : null;
        List<Message> messages = webController.getMessages()
                .parallelStream()
                .filter(element -> {
                    if (Objects.nonNull(latestStoredTime)) {
                        LocalDateTime messageTime = MessageDate.extractFrom(element);
                        if (Objects.nonNull(messageTime)) {
                            return !messageTime.isBefore(latestStoredTime);
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                })
                .map(element -> Message.fromWebElement(
                        element,
                        thread,
                        maxImageBytes.toBytes()
                ))
                .filter(Objects::nonNull)
                .filter(Message::hasComponents)
                .collect(Collectors.toList());
        if (Objects.nonNull(latestStoredMessage)) {
            int index = messages.lastIndexOf(latestStoredMessage);
            if (index >= 0) {
                return messages.subList(index + 1, messages.size());
            }
        }
        return messages;
    }

    public Stream<LocalDateTime> getMessageDates() {
        return webController.getMessages().parallelStream()
                .map(element -> {
                    try {
                        return MessageDate.extractFrom(element);
                    } catch (StaleElementReferenceException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    @Cacheable(value = "latest_messages",
            key = "#thread.id")
    public Message getLatestMessage(Thread thread) {
        return messageRepository.getLatestMessage(thread);
    }

    public Message getLatestMessage() {
        return getLatestMessage(getThread());
    }

    public Message save(Message message) {
        Message saved = messageRepository.save(message);
        log.info("Saved {}", saved.prettyPrint());
        return saved;
    }

    @Cacheable(key = "{#thread.id, #targetId}")
    public Message getMessage(Thread thread, int targetId) {
        return messageRepository.getMessage(thread, targetId);
    }

    public void sendMessage(Message message) {
        webController.sendMessage(message);
    }

    public void sendMessage(String message) {
        sendMessage(Message.fromString(webController.getThread(), webController.getMe(), message));
    }

    public void sendMessage(Text message) {
        sendMessage(Message.fromString(webController.getThread(),
                webController.getMe(),
                message.getData()));
    }

    public void sendMessageWithImage(String text, String imageUrl) {
        sendMessageWithImage(text, Image.fromUrl(imageUrl, maxImageBytes.toBytes()));
    }

    public void sendMessageWithImage(String text, MessageComponent image) {
        sendMessageWithImage(Text.fromString(text), image);
    }

    public void sendMessageWithImage(Text text, String imageUrl) {
        sendMessageWithImage(text, Image.fromUrl(imageUrl, maxImageBytes.toBytes()));
    }

    public void sendMessageWithImage(Text text, MessageComponent image) {
        Message message = Message.fromString(getThread(), webController.getMe(), text.getData());
        message.getMessageComponents().add(image);
        sendMessage(message);
    }

    public Thread getThread() {
        return webController.getThread();
    }

    public Human getMe() {
        return webController.getMe();
    }

    public RowMapper<Message> getMapper() {
        return messageRepository.getMapper();
    }

    public Message getMessageLike(Message commandMessage, String query) {
        return messageRepository.getMessageLike(commandMessage, query);
    }

    public Integer getNumberOfMessages() {
        return messageRepository.getNumberOfMessages(getThread());
    }
}
