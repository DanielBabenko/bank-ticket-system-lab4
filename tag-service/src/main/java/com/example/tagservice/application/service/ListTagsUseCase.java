package com.example.tagservice.application.service;

import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.ListTagsUseCasePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use-case для получения списка тегов (постранично).
 * Возвращает простой список доменных Tag.
 */
@Service
public class ListTagsUseCase implements ListTagsUseCasePort {

    private final TagRepositoryPort tagRepositoryPort;

    public ListTagsUseCase(TagRepositoryPort tagRepositoryPort) {
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> listAll(int page, int size) {
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
        return tagRepositoryPort.findAll(page, size);
    }
}
