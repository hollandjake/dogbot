package com.hollandjake.dogbot.service;

public interface ComponentService<T> {
    T save(T object);

    T findById(Integer id);
}
