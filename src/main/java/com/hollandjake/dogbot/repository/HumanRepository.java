package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Human;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@CacheConfig(cacheNames = "humans")
public class HumanRepository implements ComponentRepository<Human> {
    private final JdbcTemplate template;

    public HumanRepository(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.name")
    })
    public Human findById(Integer id) {
        return template.query(
                "CALL GetHuman(?)",
                getMapper(),
                id
        ).stream()
                .findAny()
                .orElse(null);
    }

    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.name")
    })
    public Human findByName(String name) {
        return template.query(
                "CALL GetOrCreateHuman(?)",
                getMapper(),
                name).stream()
                .findAny()
                .orElse(null);
    }

    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.name")
    })
    public Human findNameLike(String name) {
        return template.query(
                "CALL GetHumanWithNameLike(?)",
                getMapper(),
                name).stream()
                .findAny()
                .orElse(null);
    }

    @Override
    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.name")
    })
    public Human save(Human object) {
        if (object.getId() != null) {
            return object;
        } else {
            return findByName(object.getName());
        }
    }

    public RowMapper<Human> getMapper() {
        return (resultSet, i) ->
                Human.builder()
                        .id(resultSet.getInt("human_id"))
                        .name(resultSet.getString("name"))
                        .build();
    }
}
