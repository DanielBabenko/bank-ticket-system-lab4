package com.example.tagservice.application.service;

import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import com.example.tagservice.domain.port.inbound.CreateOrGetTagsBatchUseCasePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация батчевого use-case для создания/получения тегов.
 */
@Service
public class CreateOrGetTagsBatchUseCase implements CreateOrGetTagsBatchUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(CreateOrGetTagsBatchUseCase.class);
    private final TagRepositoryPort tagRepositoryPort;

    public CreateOrGetTagsBatchUseCase(TagRepositoryPort tagRepositoryPort) {
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    @Transactional
    public List<Tag> createOrGetTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> unique = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (unique.isEmpty()) {
            return Collections.emptyList();
        }

        List<Tag> existing = tagRepositoryPort.findByNames(unique);
        Set<String> existingNames = existing.stream().map(Tag::getName).collect(Collectors.toSet());

        List<Tag> toCreate = unique.stream()
                .filter(n -> !existingNames.contains(n))
                .map(Tag::createNew)
                .collect(Collectors.toList());

        if (!toCreate.isEmpty()) {
            List<Tag> saved = tagRepositoryPort.saveAll(toCreate);
            log.info("Created {} tags: {}", saved.size(), saved.stream().map(Tag::getName).toList());
            existing.addAll(saved);
        }

        return existing;
    }
}
