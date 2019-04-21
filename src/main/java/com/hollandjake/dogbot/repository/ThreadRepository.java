package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Thread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@CacheConfig(cacheNames = "threads")
public class ThreadRepository implements DatabaseAccessor<Thread> {
    private final JdbcTemplate template;

    public ThreadRepository(JdbcTemplate template) {
        this.template = template;
    }

    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.url")
    })
    public Thread findById(Integer id) {
        return template.query(
                "CALL GetThread(?)",
                getMapper(),
                id
        ).stream()
                .findAny()
                .orElse(null);
    }

    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.url")
    })
    public Thread findByUrl(String url) {
        return template.query(
                "CALL GetOrCreateThread(?)",
                getMapper(),
                url).stream()
                .findAny()
                .orElse(null);
    }

    @Override
    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.url")
    })
    @Transactional
    public Thread save(Thread object) {
        if (object.getId() != null) {
            return object;
        } else {
            return findByUrl(object.getUrl());
        }
    }

    public RowMapper<Thread> getMapper() {
        return (resultSet, i) ->
                Thread.builder()
                        .id(resultSet.getInt("thread_id"))
                        .url(resultSet.getString("url"))
                        .build();
    }
}
