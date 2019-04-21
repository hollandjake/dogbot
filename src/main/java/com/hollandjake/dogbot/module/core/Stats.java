package com.hollandjake.dogbot.module.core;

import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.util.CONSTANTS;
import com.hollandjake.dogbot.util.module.CommandableDatabaseModule;
import lombok.Builder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.hollandjake.dogbot.util.CONSTANTS.ACTIONIFY;
import static com.hollandjake.dogbot.util.CONSTANTS.pluralize;

public class Stats extends CommandableDatabaseModule {
    private static final String STATS_REGEX = ACTIONIFY("stats");
    private static final String UPTIME_REGEX = ACTIONIFY("uptime");
    private static final String PUPTIME_REGEX = ACTIONIFY("puptime");
    private static final DateTimeFormatter DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(CONSTANTS.DATE_TIME_FORMATTER);

    @Builder
    public Stats(MessageService messageService, JdbcTemplate template) {
        super(messageService, template);
    }

    @Override
    public boolean process(Message message, boolean freshMessage, boolean moduleOutput) {
        for (MessageComponent component : message.getMessageComponents()) {
            String match = getMatch(component);
            if (!match.isEmpty()) {
                if (moduleOutput && freshMessage) {
                    if (match.equals(STATS_REGEX)) {
                        messageService.sendMessage(getStats());
                    } else if (match.equals(UPTIME_REGEX) || match.equals(PUPTIME_REGEX)) {
                        messageService.sendMessage(getUptime());
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
            if (text.matches(STATS_REGEX)) {
                return STATS_REGEX;
            } else if (text.matches(UPTIME_REGEX)) {
                return UPTIME_REGEX;
            } else if (text.matches(PUPTIME_REGEX)) {
                return PUPTIME_REGEX;
            }
        }
        return "";
    }

    private String getMinifiedStats() {
        String version = messageService.getApplication().getVersion();
        return "Version: " + version + "\nJava version: " + System.getProperty("java.version") + "\nOperating System: "
                + System.getProperty("os.name");
    }

    private String getStats() {
        String minifiedStats = getMinifiedStats();
        return minifiedStats + "\n\n" + getUptime() +
                "\n#Messages: " + messageService.getNumberOfMessages();
    }

    private String getUptime() {
        LocalDateTime startupTime = messageService.getApplication().getStartup();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(startupTime, now);
        long diffSeconds = duration.toSecondsPart();
        long diffMinutes = duration.toMinutesPart();
        long diffHours = duration.toHoursPart();
        long diffDays = duration.toDaysPart();
        String started = DATE_TIME_FORMATTER.format(startupTime);
        return "I've been running since " + started +
                "\n[" + (diffDays > 0 ? diffDays + " day" + pluralize(diffDays) + " " : "") +
                (diffHours > 0 ? diffHours + " hour" + pluralize(diffHours) + " " : "") +
                (diffMinutes > 0 ? diffMinutes + " minute" + pluralize(diffMinutes) + " " : "") +
                diffSeconds + " second" + (diffSeconds != 1 ? "s" : "") + "]";
    }
}
