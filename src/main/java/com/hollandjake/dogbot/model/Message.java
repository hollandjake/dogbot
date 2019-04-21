package com.hollandjake.dogbot.model;

import com.hollandjake.dogbot.util.Clipbot;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Slf4j
@Builder
public class Message {
    private Integer id;

    @NonNull
    private Thread thread;

    @NonNull
    private Human sender;

    @NonNull
    private List<MessageComponent> messageComponents;

    @Transient
    private WebElement webElement;

    @NonNull
    private LocalDateTime timestamp;

    public static Message fromString(Thread thread, Human sender, String text) {
        List<MessageComponent> messageComponents = new ArrayList<>();
        messageComponents.add(Text.fromString(text));
        return Message.builder()
                .thread(thread)
                .sender(sender)
                .messageComponents(messageComponents)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static Message fromWebElement(WebElement element,
                                         Thread thread,
                                         long maxBytes) {
        try {
            Human sender = Human.getSenderFromElement(element);
            List<MessageComponent> components = MessageComponent.extractComponents(element, maxBytes);
            LocalDateTime date = MessageDate.extractFrom(element);
            return Message.builder()
                    .thread(thread)
                    .sender(sender)
                    .messageComponents(components)
                    .timestamp(date)
                    .webElement(element)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasComponents() {
        return !messageComponents.isEmpty();
    }

    public String prettyPrint() {
        return "Message #" + getId() + " {" + MessageDate.prettyPrint(timestamp) + "}, " + sender.prettyPrint() + " -> "
                + messageComponents.stream().map(MessageComponent::prettyPrint).collect(
                Collectors.joining(",", "[", "]"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        return timestamp.equals(message.timestamp) &&
                thread.equals(message.thread) &&
                sender.equals(message.sender) &&
                equalComponents(message.messageComponents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thread, sender, messageComponents, timestamp);
    }

    private boolean equalComponents(List<MessageComponent> othersComponents) {
        if (othersComponents.size() != messageComponents.size()) {
            return false;
        }

        for (int i = 0; i < othersComponents.size(); i++) {
            MessageComponent otherComponent = othersComponents.get(i);
            MessageComponent myComponent = messageComponents.get(i);
            if (!myComponent.equals(otherComponent)) {
                return false;
            }
        }
        return true;
    }

    public void send(WebElement inputBox, WebDriverWait wait, Clipbot clipbot) {
        try {
            clipbot.cache();
            messageComponents.forEach(component -> component.send(inputBox, wait, clipbot));
            inputBox.sendKeys(" " + Keys.ENTER);
        } finally {
            clipbot.flush();
        }
    }
}
