package com.hollandjake.dogbot.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@CacheConfig(cacheNames = "modules")
public class ModuleRepository implements DatabaseAccessor<Object> {
    private final JdbcTemplate template;

    public ModuleRepository(JdbcTemplate template) {
        this.template = template;
    }

    @CachePut(key = "#moduleName")
    public Integer findByName(String moduleName) {
        return template.queryForObject(
                "SELECT GetOrCreateModuleId(?)",
                Integer.class,
                moduleName);
    }

    public Boolean doesModuleExist(String moduleName) {
        return Optional.ofNullable(template.queryForObject(
                "SELECT module_id FROM module where module_name = ? LIMIT 1",
                ((rs, rowNum) -> true),
                moduleName)).orElse(false);
    }

    @Override
    @CachePut(key = "#moduleName")
    @Transactional
    public Integer save(Object moduleName) {
        if (moduleName instanceof String) {
            return findByName((String) moduleName);
        } else {
            return null;
        }
    }

    @Override
    public RowMapper<Object> getMapper() {
        return null;
    }
}
