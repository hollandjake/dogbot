package com.hollandjake.dogbot.repository;

public interface ComponentRepository<T> extends DatabaseAccessor<T> {
    T findById(Integer id);
}
