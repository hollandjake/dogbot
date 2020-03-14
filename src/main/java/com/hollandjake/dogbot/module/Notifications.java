package com.hollandjake.dogbot.module;

import com.hollandjake.dogbot.model.*;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.service.NotificationService;
import com.hollandjake.dogbot.util.exceptions.FailedToSaveException;
import com.hollandjake.dogbot.util.exceptions.MalformedCommandException;
import com.hollandjake.dogbot.util.module.CommandableDatabaseNotifiableModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;

@Slf4j
public class Notifications extends CommandableDatabaseNotifiableModule {
    private static final String EVENT_REGEX = ACTIONIFY("(event|deadline)s");
    private static final String NOTIFICATION_REGEX = ACTIONIFY("(notification)s");
    private static final String ADD_EVENT_REGEX = ACTIONIFY("add(event|deadline) (.+)-(.+)");
    private static final String ADD_NOTIFICATION_REGEX = ACTIONIFY("add(notification) (.+)-(.+)");

    public Notifications(MessageService messageService,
                         NotificationService notificationService,
                         JdbcTemplate template) {
        super(messageService, notificationService, template);
    }

    private static boolean isTimeInFuture(LocalDateTime time) {
        return time.isAfter(LocalDateTime.now());
    }

    private void sendAllUnsentEvents() {
        List<Event> events = notificationService.getAllUnsentEvents(messageService.getThread());
        if (events.isEmpty()) {
            messageService.sendMessage("This chat has no active notifications");
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Events:");
            events.forEach(notification -> message.append("\n\n").append(notification.generateMessage()));
            messageService.sendMessage(message.toString());
        }
    }

    private void sendAllUnsentNotification() {
        List<Notification> notifications = notificationService.getAllUnsentNotifications(messageService.getThread());
        if (notifications.isEmpty()) {
            messageService.sendMessage("This chat has no active notifications");
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Notifications:");
            notifications.forEach(notification -> message.append("\n\n").append(notification.generateMessage(false)));
            messageService.sendMessage(message.toString());
        }
    }

    private LocalDateTime parseTime(String time) {
        time = time.trim();
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                Notification.FORMATTER,
                Notification.FORMATTER_NO_TIME
        );

        String queryTime = time;
        return formatters.stream()
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
                .orElseGet(() -> {
                    messageService.sendMessage("Failed to read the time '" + queryTime + "'");
                    return null;
                });
    }

    private Notification addNotification(LocalDateTime time, LocalDateTime showTime, String text) {
        if (isTimeInFuture(time)) {
            try {
                return notificationService.save(
                        Notification
                                .builder()
                                .thread(messageService.getThread())
                                .time(time)
                                .showTime(showTime)
                                .message(text)
                                .build()
                );
            } catch (FailedToSaveException e) {
                log.error("Failed to save notification", e);
                messageService.sendMessage(e.getMessage());
            }
        } else {
            messageService.sendMessage(time.format(Notification.FORMATTER) + " is in the past");
        }
        return null;
    }

    private void addNotification(String time, String text) {
        String queryTime = time.trim();
        String queryText = text.trim();
        LocalDateTime notificationTime = parseTime(queryTime);
        if (Objects.nonNull(notificationTime)) {
            Notification notification = addNotification(notificationTime, notificationTime, queryText);
            if (Objects.nonNull(notification)) {
                messageService.sendMessage("Added event:" + "\n" + notification.generateMessage(true));
            }
        }
    }

    private void addEvent(String time, String text) {
        String queryTime = time.trim();
        String queryText = text.trim();
        LocalDateTime eventTime = parseTime(queryTime);
        if (Objects.nonNull(eventTime)) {
            Event event = Event.builder()
                    .thread(messageService.getThread())
                    .time(eventTime)
                    .message(queryText)
                    .build();
            if (isTimeInFuture(eventTime)) {
                try {
                    notificationService.save(event);
                } catch (FailedToSaveException e) {
                    log.error("Failed to save event", e);
                    messageService.sendMessage(e.getMessage());
                }
            } else {
                messageService.sendMessage(eventTime.format(Notification.FORMATTER) + " is in the past");
            }


            if (Objects.nonNull(event)) {
                messageService.sendMessage("Added event:" + "\n" + event.generateMessage());
            }
        }
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) throws Exception {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(EVENT_REGEX)) {
                        sendAllUnsentEvents();
                    } else if (match.equals(NOTIFICATION_REGEX)) {
                        sendAllUnsentNotification();
                    } else if (match.equals(ADD_NOTIFICATION_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(ADD_NOTIFICATION_REGEX).matcher(text);
                        if (matcher.find() && matcher.group(2) != null && matcher.group(3) != null) {
                            String time = matcher.group(2);
                            String notificationMessage = matcher.group(3);
                            addNotification(time, notificationMessage);
                        } else {
                            throw new MalformedCommandException();
                        }
                    } else if (match.equals(ADD_EVENT_REGEX)) {
                        String text = ((Text) component).getData();
                        Matcher matcher = Pattern.compile(ADD_EVENT_REGEX).matcher(text);
                        if (matcher.find() && matcher.group(2) != null && matcher.group(3) != null) {
                            String time = matcher.group(2);
                            String eventMessage = matcher.group(3);
                            addEvent(time, eventMessage);
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
    @SuppressWarnings("Duplicates")
    public String getMatch(MessageComponent component) {
        if (component instanceof Text) {
            String text = ((Text) component).getData();
            if (text.matches(EVENT_REGEX)) {
                return EVENT_REGEX;
            } else if (text.matches(ADD_EVENT_REGEX)) {
                return ADD_EVENT_REGEX;
            } else if (text.matches(NOTIFICATION_REGEX)) {
                return NOTIFICATION_REGEX;
            } else if (text.matches(ADD_NOTIFICATION_REGEX)) {
                return ADD_NOTIFICATION_REGEX;
            }
        }
        return "";
    }
}