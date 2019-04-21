package com.hollandjake.dogbot.model;

import com.google.errorprone.annotations.ForOverride;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Slf4j
@AllArgsConstructor
@RequiredArgsConstructor
public abstract class MessageComponent implements MessageObject {
    private Integer id;

    @ForOverride
    public static Set<MessageComponent> extractFrom(WebElement messageElement) {
        throw new IllegalArgumentException("Subclass did not declare an overridden extractFrom() method.");
    }

    static List<MessageComponent> extractComponents(WebElement messageElement, long maxBytes) {
        List<MessageComponent> messageComponents = new ArrayList<>();

        //Check what type it is
        messageComponents.addAll(Image.extractFrom(messageElement, maxBytes));
        messageComponents.addAll(Text.extractFrom(messageElement));

        return messageComponents;
    }
}
