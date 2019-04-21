package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.repository.TextRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@CacheConfig(cacheNames = "text")
public class TextService implements ComponentService<Text> {
    @Getter
    private final TextRepository textRepository;

    @Autowired
    public TextService(TextRepository textRepository) {
        this.textRepository = textRepository;
    }

    @Cacheable(key = "#id")
    public Text findById(Integer id) {
        return textRepository.findById(id);
    }

    @Cacheable(key = "#text")
    public Text findByText(String text) {
        return textRepository.findByText(text);
    }

    @Cacheable(key = "#object.data")
    @Transactional
    public Text save(Text object) {
        return textRepository.save(object);
    }

    public Text getRandomResponseForModule(Integer moduleId) {
        return textRepository.getRandomResponseForModule(moduleId);
    }
}
