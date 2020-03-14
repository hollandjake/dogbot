package com.hollandjake.dogbot.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
@Slf4j
public class Notification {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("ccc, d LLL YYYY HH:mm");
    public static final DateTimeFormatter FORMATTER_NO_TIME = DateTimeFormatter.ofPattern("ccc, d LLL YYYY");

    private Integer id;
    @NonNull
    private Thread thread;
    @NonNull
    private LocalDateTime time;
    @NonNull
    private LocalDateTime showTime;
    @NonNull
    private String message;
    @NonNull
    private Boolean sent;

    @Builder
    public Notification(Integer id, Thread thread, LocalDateTime time, LocalDateTime showTime, String message, Boolean sent) {
        this.id = id;
        this.thread = thread;
        this.time = time;
        this.showTime = showTime;
        this.message = message;
        this.sent = Objects.nonNull(sent) && sent;
    }

    private static String formatTime(LocalDateTime time) {
        if (LocalTime.MIDNIGHT.equals(time.toLocalTime())) {
            return time.format(FORMATTER_NO_TIME);
        } else {
            return time.format(FORMATTER);
        }
    }

    public String generateMessage(boolean bothTimes) {
        return "[" + (bothTimes && !time.equals(showTime) ? formatTime(time) + " : " : "") + formatTime(showTime) + "]\n" + message;
    }
}
