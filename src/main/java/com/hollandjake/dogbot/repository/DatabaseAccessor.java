package com.hollandjake.dogbot.repository;

import org.springframework.jdbc.core.RowMapper;

public interface DatabaseAccessor<T> {
    T save(T object);

    RowMapper<T> getMapper();
}
