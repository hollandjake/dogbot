package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Notification;
import com.hollandjake.dogbot.model.Thread;
import com.hollandjake.dogbot.service.ThreadService;
import com.hollandjake.dogbot.util.exceptions.FailedToSaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Repository
public class NotificationRepository implements DatabaseAccessor<Notification> {
    private final ThreadService threadService;
    private final JdbcTemplate template;


    @Autowired
    public NotificationRepository(ThreadService threadService, JdbcTemplate template) {
        this.threadService = threadService;
        this.template = template;
    }

    @Override
    public Notification save(Notification notification) {
        List<Notification> query = template.query(
                "CALL SaveNotification(?, ?, ?, ?, ?)",
                getMapper(),
                notification.getThread().getId(),
                notification.getMessage(),
                Timestamp.valueOf(notification.getTime()),
                Timestamp.valueOf(notification.getShowTime()),
                notification.getSent()
        );
        return query.stream().findAny().orElseThrow(() -> new FailedToSaveException("Failed to save notification"));
    }

    @Override
    public RowMapper<Notification> getMapper() {
        return (resultSet, i) ->
                Notification.builder()
                        .id(resultSet.getInt("notification_id"))
                        .thread(threadService.findById(resultSet.getInt("thread_id")))
                        .message(resultSet.getString("message"))
                        .time(resultSet.getTimestamp("time").toLocalDateTime())
                        .showTime(resultSet.getTimestamp("show_time").toLocalDateTime())
                        .sent(resultSet.getBoolean("sent"))
                        .build();
    }

    public List<Notification> getAllUnsentNotifications(Thread thread) {
        return template.query("SELECT notification_id, thread_id, message, time, show_time, sent " +
                        "FROM notification " +
                        "WHERE thread_id = ? " +
                        "AND NOT sent",
                getMapper(),
                thread.getId()
        );
    }
}
