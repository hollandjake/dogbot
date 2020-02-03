package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Human;
import com.hollandjake.dogbot.model.Message;
import com.hollandjake.dogbot.model.Thread;
import com.hollandjake.dogbot.service.HumanService;
import com.hollandjake.dogbot.service.MessageComponentService;
import com.hollandjake.dogbot.service.ThreadService;
import com.hollandjake.dogbot.util.exceptions.FailedToSaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Slf4j
@Repository
@CacheConfig(cacheNames = "messages")
public class MessageRepository implements DatabaseAccessor<Message> {
    private final JdbcTemplate template;
    private final ThreadService threadService;
    private final HumanService humanService;
    private final MessageComponentService messageComponentService;

    @Autowired
    public MessageRepository(JdbcTemplate template,
                             ThreadService threadService,
                             MessageComponentService messageComponentService,
                             HumanService humanService) {
        this.template = template;
        this.threadService = threadService;
        this.humanService = humanService;
        this.messageComponentService = messageComponentService;
    }

    @Caching(put = {
            @CachePut(value = "latest_messages",
                    key = "#result.thread.id"),
            @CachePut(key = "{#result.thread.id, #result.id}")
    })
    public Message getLatestMessage(Thread thread) {
        return template.query(
                "SELECT * FROM message WHERE thread_id = ? ORDER BY message_id DESC LIMIT 1",
                getMapper(),
                thread.getId()
        ).stream().findAny().orElse(null);
    }

    @Override
    @CachePut(key = "{#result.thread.id, #result.id}")
    @CacheEvict(value = "latest_messages",
            key = "#result.thread.id")
    @Transactional
    public Message save(Message object) {
        Thread savedThread = threadService.save(object.getThread());
        Human savedSender = humanService.save(object.getSender());
        return save(object, savedThread, savedSender);
    }

    @Transactional
    public Message save(Message toSave, Thread savedThread, Human savedSender) {
        if (toSave.getId() != null) {
            return toSave;
        } else {
            Message saved = template.query(
                    "CALL SaveMessage(?, ?, ?)",
                    getMapper(),
                    savedThread.getId(),
                    savedSender.getId(),
                    Timestamp.valueOf(toSave.getTimestamp())
            ).stream().findAny().orElseThrow(() -> new FailedToSaveException("Failed to save message"));
            saved.setThread(savedThread);
            saved.setSender(savedSender);
            saved.setMessageComponents(
                    messageComponentService.save(
                            toSave.getMessageComponents(),
                            saved.getThread().getId(),
                            saved.getId()
                    )
            );
            return saved;
        }
    }

    @CachePut(key = "{#result.thread.id, #result.id}")
    public Message getMessage(Thread thread, int targetId) {
        return template.query(
                "SELECT * FROM message WHERE thread_id = ? AND message_id = ? LIMIT 1",
                getMapper(),
                thread.getId(),
                targetId
        ).stream().findAny().orElse(null);
    }

    public Message getMessageLike(Message commandMessage, String query) {
        return template.query(
                "CALL GetMessageLike(?,?,?)",
                getMapper(),
                commandMessage.getThread().getId(),
                commandMessage.getId(),
                query).stream().findAny().orElse(null);
    }

    public RowMapper<Message> getMapper() {
        return (resultSet, i) ->
                Message.builder()
                        .id(resultSet.getInt("message_id"))
                        .timestamp(resultSet.getTimestamp("timestamp").toLocalDateTime())
                        .sender(humanService.findById(resultSet.getInt("sender_id")))
                        .thread(threadService.findById(resultSet.getInt("thread_id")))
                        .messageComponents(
                                messageComponentService.findByMessage(
                                        resultSet.getInt("thread_id"),
                                        resultSet.getInt("message_id")
                                )
                        )
                        .build();
    }

    public Integer getNumberOfMessages(Thread thread) {
        return template.queryForObject(
                "SELECT COUNT(*) FROM message WHERE thread_id = ?",
                Integer.class,
                thread.getId()
        );
    }
}
