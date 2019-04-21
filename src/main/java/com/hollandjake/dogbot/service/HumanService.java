package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.model.Human;
import com.hollandjake.dogbot.repository.HumanRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@CacheConfig(cacheNames = "humans")
public class HumanService implements ComponentService<Human> {
    @Getter
    private final HumanRepository humanRepository;

    @Autowired
    public HumanService(HumanRepository humanRepository) {
        this.humanRepository = humanRepository;
    }

    @Cacheable(key = "#id")
    public Human findById(Integer id) {
        return humanRepository.findById(id);
    }

    public Human findNameLike(String name) {
        return humanRepository.findNameLike(name);
    }

    @Cacheable(key = "#name")
    public Human findByName(String name) {
        return humanRepository.findByName(name);
    }

    @Cacheable(key = "#object.name")
    @Transactional
    public Human save(Human object) {
        return humanRepository.save(object);
    }
}
