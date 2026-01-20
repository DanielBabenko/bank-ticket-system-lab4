package com.example.tagservice.application.service;

import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import com.example.tagservice.domain.port.inbound.CreateTagUseCasePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация use-case: createIfNotExists.
 * Название класса совпадает с договорённым именованием (CreateTagUseCase),
 * находится в application.service и реализует входной порт из domain.
 */
@Service
public class CreateTagUseCase implements CreateTagUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(CreateTagUseCase.class);
    private final TagRepositoryPort tagRepositoryPort;

    public CreateTagUseCase(TagRepositoryPort tagRepositoryPort) {
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    @Transactional
    public Tag createIfNotExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name required");
        }
        String trimmed = name.trim();

        return tagRepositoryPort.findByName(trimmed)
                .orElseGet(() -> {
                    Tag newTag = Tag.createNew(trimmed);
                    Tag saved = tagRepositoryPort.save(newTag);
                    log.info("Created tag: {}", saved.getName());
                    return saved;
                });
    }
}
