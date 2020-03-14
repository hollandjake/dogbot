package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Event;
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
public class EventRepository implements DatabaseAccessor<Object> {
    private final ThreadService threadService;
    private final JdbcTemplate template;


    @Autowired
    public EventRepository(ThreadService threadService, JdbcTemplate template) {
        this.threadService = threadService;
        this.template = template;
    }

    @Override
    public Integer save(Object obj) {
        if (obj instanceof Event) {
            Event event = (Event) obj;
            List<Integer> query = template.queryForList(
                    "SELECT SaveEvent(?, ?, ?)",
                    Integer.class,
                    event.getThread().getId(),
                    event.getMessage(),
                    Timestamp.valueOf(event.getTime())
            );
            return query.stream().findAny().orElseThrow(() -> new FailedToSaveException("Failed to save event"));
        } else {
            throw new FailedToSaveException("Invalid object type");
        }
    }

    @Override
    public RowMapper<Object> getMapper() {
        return null;
    }

    public RowMapper<Event> getEventMapper() {
        return (resultSet, i) ->
                Event.builder()
                        .id(resultSet.getInt("event_id"))
                        .thread(threadService.findById(resultSet.getInt("thread_id")))
                        .message(resultSet.getString("message"))
                        .time(resultSet.getTimestamp("time").toLocalDateTime())
                        .build();
    }

    public List<Event> getAllUnsentEvents(Thread thread) {
        return template.query("SELECT DISTINCT e.event_id, e.thread_id, e.message, e.time " +
                        "FROM event e " +
                        "JOIN event_notification en on e.event_id = en.event_id " +
                        "JOIN notification n on en.notification_id = n.notification_id " +
                        "WHERE e.thread_id = ? " +
                        "AND NOT n.sent",
                getEventMapper(),
                thread.getId()
        );
    }
}
