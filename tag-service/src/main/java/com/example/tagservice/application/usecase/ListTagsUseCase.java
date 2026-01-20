package com.example.tagservice.application.usecase;

import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.ListTagsUseCasePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;

import java.util.List;

/**
 * Use-case для получения списка тегов (постранично).
 * Возвращает простой список доменных Tag.
 */
public class ListTagsUseCase implements ListTagsUseCasePort {

    private final TagRepositoryPort tagRepositoryPort;

    public ListTagsUseCase(TagRepositoryPort tagRepositoryPort) {
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    public List<Tag> listAll(int page, int size) {
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
        return tagRepositoryPort.findAll(page, size);
    }
}
