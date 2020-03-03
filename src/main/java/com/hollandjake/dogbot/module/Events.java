package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

public class Events extends CommandableDatabaseModule {
    private static final String EVENT_REGEX = ACTIONIFY("(event|deadline)s?");
    private static final String ADD_EVENT_REGEX = ACTIONIFY("add(event|deadline) (.+)-(.+)");

    public Events(MessageService messageService,
                  JdbcTemplate template) {
        super(messageService, template);
    }

    private void getAllEvents() {
        List<Map<String, Object>> events = template.queryForList(
                "SELECT DISTINCT message, time FROM events WHERE thread_id = ? AND time > NOW() ORDER BY time",
                messageService.getThread().getId()
        );
        if (events.isEmpty()) {
            messageService.sendMessage("This chat has no active events");
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Events:");
            events.forEach(event -> message.append("\n[").append(parseTime((Timestamp) event.get("time"))).append("] : ").append(event.get("message")));
            messageService.sendMessage(message.toString());
        }
    }

    private void addEvent(Message message, String time, String text) throws MalformedCommandException {
        time = time.trim();
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );

        String queryTime = time;
        formatters.stream()
                .map(f -> {
                    try {
                        return LocalDateTime.parse(queryTime, f);
                    } catch (DateTimeParseException e) {
                        try {
                            return LocalDateTime.of(LocalDate.parse(queryTime, f), LocalTime.MIDNIGHT);
                        } catch (DateTimeParseException ex) {
                            return null;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .findAny()
                .ifPresentOrElse(
                        eventTime -> {
                            if (eventTime.isAfter(LocalDateTime.now())) {
                                Timestamp timestamp = Timestamp.valueOf(eventTime);
                                template.update("INSERT INTO events (thread_id, message, time) VALUES (?, ?, ?)",
                                        message.getThread().getId(),
                                        text,
                                        timestamp
                                );
                                messageService.sendMessage("Added event:" + "\n[" + parseTime(timestamp) + "] : " + text);
                            } else {
                                messageService.sendMessage(queryTime + " was in the past");
                            }
                        },
                        () -> messageService.sendMessage("Failed to read the time '" + queryTime + "'")
                );
    }

    private String parseTime(Timestamp timestamp) {
        LocalDateTime t = timestamp.toLocalDateTime();
        StringBuilder dateTime = new StringBuilder()
                .append(t.getDayOfMonth()).append(" ")
                .append(t.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)).append(" ")
                .append(t.getYear());

        LocalTime time = LocalTime.of(t.getHour(), t.getMinute(), t.getSecond());
        if (!time.equals(LocalTime.MIDNIGHT)) {
            dateTime.append(" ");
            if (time.getHour() > 0) {
                dateTime.append(time.getHour());
            }
            if (time.getMinute() > 0) {
                if (time.getHour() <= 0) {
                    dateTime.append("00");
                }
                dateTime.append(":").append(time.getMinute());
            }
            if (time.getSecond() > 0) {
                if (time.getMinute() <= 0) {
                    dateTime.append("00");
                    if (time.getHour() <= 0) {
                        dateTime.append(":00");
                    }
                }
                dateTime.append(":").append(time.getSecond());
            }
        }
        return dateTime.toString();
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws Exception {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(EVENT_REGEX)) {
                        getAllEvents();
                    } else if (match.equals(ADD_EVENT_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(ADD_EVENT_REGEX).matcher(text);
                        if (matcher.find() && matcher.group(2) != null && matcher.group(3) != null) {
                            String time = matcher.group(2);
                            String m = matcher.group(3);
                            addEvent(message, time, m);
                        } else {
                            throw new MalformedCommandException();
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            if (text.matches(EVENT_REGEX)) {
                return EVENT_REGEX;
            } else if (text.matches(ADD_EVENT_REGEX)) {
                return ADD_EVENT_REGEX;
            }
        }
        return "";
    }
}