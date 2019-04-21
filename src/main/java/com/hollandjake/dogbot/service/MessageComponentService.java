package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.model.Human;
import com.hollandjake.dogbot.model.Image;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.repository.MessageComponentRepository;
import com.hollandjake.dogbot.util.exceptions.FailedToSaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageComponentService {
    private final MessageComponentRepository messageComponentRepository;
    private final TextService textService;
    private final ImageService imageService;
    private final HumanService humanService;

    @Autowired
    public MessageComponentService(
            MessageComponentRepository messageComponentRepository,
            TextService textService,
            ImageService imageService,
            HumanService humanService) {
        this.messageComponentRepository = messageComponentRepository;
        this.textService = textService;
        this.imageService = imageService;
        this.humanService = humanService;
    }

    public List<MessageComponent> findByMessage(Integer threadId, Integer messageId) {
        return messageComponentRepository.findByMessage(threadId, messageId);
    }

    @Transactional
    public List<MessageComponent> save(List<MessageComponent> messageComponents, Integer threadId, Integer messageId) {
        return messageComponents.stream()
                .map(x -> saveForMessage(x, threadId, messageId))
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageComponent saveForMessage(MessageComponent object, Integer threadId, Integer messageId) {
        MessageComponent saved;
        if (object instanceof Text) {
            saved = textService.save((Text) object);
        } else if (object instanceof Image) {
            saved = imageService.save((Image) object);
        } else if (object instanceof Human) {
            saved = humanService.save((Human) object);
        } else {
            throw new FailedToSaveException("Invalid component");
        }
        messageComponentRepository.saveForMessage(saved, threadId, messageId);
        return saved;
    }
}
