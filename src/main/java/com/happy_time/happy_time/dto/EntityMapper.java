package com.happy_time.happy_time.dto;

public interface EntityMapper<D, E> {
    D map(E entity);
    void map(E gift, D dto);
}
