package com.hollandjake.dogbot.model;

import com.google.errorprone.annotations.ForOverride;
import com.hollandjake.dogbot.util.Clipbot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public interface MessageObject {
    @ForOverride
    static Object extractFrom(WebElement messageElement) {
        throw new IllegalArgumentException("Subclass did not declare an overridden extractFrom() method.");
    }

    String prettyPrint();

    void send(WebElement inputBox, WebDriverWait wait, Clipbot clipbot);
}
