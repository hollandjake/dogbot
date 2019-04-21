package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.model.Thread;
import com.hollandjake.dogbot.repository.ThreadRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@CacheConfig(cacheNames = "threads")
public class ThreadService implements ComponentService<Thread> {
    @Getter
    private final ThreadRepository threadRepository;

    @Autowired
    public ThreadService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    @Cacheable(key = "#id")
    public Thread findById(Integer id) {
        return threadRepository.findById(id);
    }

    @Cacheable(key = "#url")
    public Thread findByUrl(String url) {
        return threadRepository.findByUrl(url);
    }

    @Cacheable(key = "#object.url")
    public Thread save(Thread object) {
        return threadRepository.save(object);
    }
}
