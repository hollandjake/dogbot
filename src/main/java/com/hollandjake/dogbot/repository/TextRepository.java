package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Text;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@CacheConfig(cacheNames = "text")
public class TextRepository implements ComponentRepository<Text> {
    private final JdbcTemplate template;


    @Autowired
    public TextRepository(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.data")
    })
    public Text findById(Integer id) {
        return template.query(
                "CALL GetText(?)",
                getMapper(),
                id
        ).stream()
                .findAny()
                .orElse(null);
    }

    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.data")
    })
    public Text findByText(String text) {
        return template.query(
                "CALL GetOrCreateText(?)",
                getMapper(),
                text).stream()
                .findAny()
                .orElse(null);
    }

    @Override
    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.data")
    })
    @Transactional
    public Text save(Text object) {
        if (object.getId() != null) {
            return object;
        } else {
            return findByText(object.getData());
        }
    }

    public RowMapper<Text> getMapper() {
        return (resultSet, i) ->
                Text.builder()
                        .id(resultSet.getInt("text_id"))
                        .data(resultSet.getString("data"))
                        .build();
    }

    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.data")
    })
    public Text getRandomResponseForModule(Integer moduleId) {
        return Optional.ofNullable(template.queryForObject(
                "SELECT t.text_id, data "
                        + "FROM text t "
                        + "JOIN module_text mt on t.text_id = mt.text_id "
                        + "WHERE module_id = ? "
                        + "ORDER BY RAND() "
                        + "LIMIT 1",
                getMapper(),
                moduleId)
        ).orElseGet(() -> Text.fromString("Text couldn't load"));
    }
}
