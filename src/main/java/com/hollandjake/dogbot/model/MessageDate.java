package com.hollandjake.dogbot.model;

import com.hollandjake.dogbot.util.CONSTANTS;
import com.hollandjake.dogbot.util.Clipbot;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.awt.datatransfer.StringSelection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.XPATHS.MESSAGE_DATE;

@Slf4j
public abstract class MessageDate implements MessageObject {
    private static final DateTimeFormatter MESSAGE_DATE_FORMATTER
            = DateTimeFormatter.ofPattern(CONSTANTS.MESSAGE_DATE_FORMATTER);
    private static final DateTimeFormatter MESSAGE_DATE_SINGULAR_FORMATTER
            = DateTimeFormatter.ofPattern(CONSTANTS.MESSAGE_DATE_SINGULAR_FORMATTER);
    private static Pattern TODAY_REGEX = Pattern.compile("(\\d\\d):(\\d\\d)");
    private static Pattern OTHER_DAY_REGEX = Pattern.compile("(\\S+) (\\d\\d):(\\d\\d)");

    private MessageDate() {
    }

    public static LocalDateTime extractFrom(WebElement messageElement) {
        List<WebElement> dateElements = messageElement.findElements(By.xpath(MESSAGE_DATE));
        LocalDateTime dateTime;
        if (!dateElements.isEmpty()) {
            WebElement dateElement = dateElements.get(0);
            String dateString = dateElement.getAttribute("data-tooltip-content").replace("at ", "");
            Matcher matcher = TODAY_REGEX.matcher(dateString);
            if (matcher.matches()) {
                //Message is from today
                dateTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.of(
                                Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2))
                        )
                );
            } else {
                matcher = OTHER_DAY_REGEX.matcher(dateString);
                if (matcher.matches()) {
                    //Message is from this week
                    DayOfWeek dayOfWeek = DayOfWeek.valueOf(matcher.group(1).toUpperCase());

                    dateTime = LocalDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(
                                    Integer.parseInt(matcher.group(2)),
                                    Integer.parseInt(matcher.group(3))
                            )
                    ).with(TemporalAdjusters.previous(dayOfWeek));
                } else {
                    try {
                        dateTime = LocalDateTime.parse(dateString, MESSAGE_DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        try {
                            dateTime = LocalDateTime.parse(dateString, MESSAGE_DATE_SINGULAR_FORMATTER);
                        } catch (DateTimeParseException x) {
                            return null;
                        }
                    }
                }
            }
            return dateTime;
        } else {
            return null;
        }
    }

    public static String prettyPrint(LocalDateTime date) {
        return MESSAGE_DATE_FORMATTER.format(date);
    }

    public static void send(WebElement inputBox, LocalDateTime date, Clipbot clipbot) {
        clipbot.paste(new StringSelection("[" + prettyPrint(date) + "]"), inputBox);
    }
}
