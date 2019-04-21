package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Human;
import com.hollandjake.dogbot.model.Image;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.HumanService;
import com.hollandjake.dogbot.service.ImageService;
import com.hollandjake.dogbot.service.TextService;
import com.hollandjake.dogbot.util.exceptions.FailedToFetchException;
import com.hollandjake.dogbot.util.exceptions.FailedToSaveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@CacheConfig(cacheNames = "message_components")
public class MessageComponentRepository {
    private final JdbcTemplate template;
    private final ImageService imageService;
    private final HumanService humanService;
    private final TextService textService;

    @Autowired
    public MessageComponentRepository(
            JdbcTemplate template,
            ImageService imageService,
            HumanService humanService,
            TextService textService) {
        this.template = template;
        this.imageService = imageService;
        this.humanService = humanService;
        this.textService = textService;
    }

    @CachePut(key = "{#threadId, #messageId}")
    public List<MessageComponent> findByMessage(Integer threadId, Integer messageId) {
        return template.query(
                "SELECT * FROM message_component WHERE thread_id = ? AND message_id = ?",
                getMapper(),
                threadId,
                messageId
        );
    }

    public void saveForMessage(MessageComponent object, Integer threadId, Integer messageId) {
        String tableField;
        if (object instanceof Text) {
            tableField = "text_id";
        } else if (object instanceof Image) {
            tableField = "image_id";
        } else if (object instanceof Human) {
            tableField = "human_id";
        } else {
            throw new FailedToSaveException("Invalid component to add to message");
        }

        try {
            template.update(
                    "INSERT INTO message_component (thread_id, message_id, " + tableField + ") VALUE (?, ?, ?)",
                    threadId,
                    messageId,
                    object.getId()
            );
        } catch (DataAccessException e) {
            throw new FailedToSaveException("Failed to add " + object.getClass().getSimpleName() + " to message", e);
        }
    }

    public RowMapper<MessageComponent> getMapper() {
        return ((resultSet, i) -> {
            int textId = resultSet.getInt("text_id");
            int imageId = resultSet.getInt("image_id");
            int humanId = resultSet.getInt("human_id");
            if (textId > 0) {
                return textService.findById(
                        textId
                );
            } else if (imageId > 0) {
                return imageService.findById(
                        imageId
                );
            } else if (humanId > 0) {
                return imageService.findById(
                        humanId
                );
            } else {
                throw new FailedToFetchException("Failed to fetch component from database");
            }
        });
    }
}
