package com.hollandjake.dogbot.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Keys;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class CONSTANTS {
    public static final String DATE_TIME_FORMATTER = "yy/MM/dd HH:mm:ss";
    public static final String MESSAGE_DATE_FORMATTER = "dd MMMM yyyy HH:mm";
    public static final String MESSAGE_DATE_SINGULAR_FORMATTER = "d MMMM yyyy HH:mm";
    public static final String MESSAGE_TIMEOUT_FORMATTER = "HH:mm:ss";
    //region System
    public static final String COPY = Keys.chord(Keys.CONTROL, "c");
    public static final String PASTE = Keys.chord(Keys.CONTROL, "v");
    //region Messenger
    public static final String MESSENGER_URL = "https://www.messenger.com/t/";
    //endregion
    //endregion
    public static final Random RANDOM = new Random();

    private CONSTANTS() {
    }

    public static String ACTIONIFY(String arg) {
        return "(?i)^!\\s*" + arg + "$";
    }

    public static String ACTIONIFY_CASE(String arg) {
        return "^!\\s*" + arg + "$";
    }

    public static String GET_PAGE_SOURCE(String url) {
        try {
            return Unirest.get(url).header("User-agent", "Chatbot").asString().getBody();
        } catch (UnirestException e) {
            log.error("Page doesn't exist", e);
            return "";
        }
    }

    public static String pluralize(long i) {
        return i != 1 ? "s" : "";
    }

    public static <T> T GET_RANDOM(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}