package com.hollandjake.dogbot.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.hollandjake.dogbot.model.Notification.FORMATTER;
import static com.hollandjake.dogbot.model.Notification.FORMATTER_NO_TIME;

@Data
@Slf4j
public class Event {
    private Integer id;
    @NonNull
    private Thread thread;
    @NonNull
    private LocalDateTime time;
    @NonNull
    private String message;

    @Builder
    public Event(Integer id, Thread thread, LocalDateTime time, String message) {
        this.id = id;
        this.thread = thread;
        this.time = time;
        this.message = message;
    }

    private static String formatTime(LocalDateTime time) {
        if (LocalTime.MIDNIGHT.equals(time.toLocalTime())) {
            return time.format(FORMATTER_NO_TIME);
        } else {
            return time.format(FORMATTER);
        }
    }

    public String generateMessage() {
        return "[" + formatTime(time) + "]\n" + message;
    }
}
