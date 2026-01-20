package com.example.tagservice.domain.port.inbound;

import com.example.tagservice.domain.model.Tag;

import java.util.List;

/**
 * Use-case для получения списка тегов (постранично).
 * Здесь в домене мы используем простую сигнатуру: page + size -> список.
 * (В application-слое можно обернуть в DTO вместе с totalCount при необходимости.)
 */
public interface ListTagsUseCasePort {
    List<Tag> listAll(int page, int size);
}
