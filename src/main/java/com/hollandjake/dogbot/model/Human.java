package com.hollandjake.dogbot.model;

import com.hollandjake.dogbot.util.Clipbot;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.datatransfer.StringSelection;

import static com.hollandjake.dogbot.util.XPATHS.MENTIONS;
import static com.hollandjake.dogbot.util.XPATHS.MESSAGE_SENDER;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Human extends MessageComponent {
    @NonNull
    private String name;

    @Builder
    public Human(Integer id, String name) {
        super(id);
        this.name = name;
    }

    public static Human getSenderFromElement(WebElement element) {
        WebElement humanContainer = element.findElement(By.xpath(MESSAGE_SENDER));
        String name = humanContainer.getAttribute("data-tooltip-content");
        return Human.builder().name(name).build();
    }

    @Override
    public String prettyPrint() {
        return "@" + name;
    }

    @Override
    public void send(WebElement inputBox, WebDriverWait wait, Clipbot clipbot) {
        clipbot.paste(new StringSelection(prettyPrint()), inputBox);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MENTIONS)));
        inputBox.sendKeys(Keys.ENTER);
    }
}
