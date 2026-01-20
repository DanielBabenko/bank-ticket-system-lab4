package com.example.tagservice.application.service;

import com.example.tagservice.application.exception.NotFoundException;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.GetTagUseCasePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use-case для получения тега по имени.
 * Реализация возвращает доменную сущность Tag (без внешних applications).
 * Адаптер (контроллер) при необходимости может дополнительно запросить
 * application-service через соответствующий outbound-адаптер и собрать TagDto.
 */
@Service
public class GetTagUseCase implements GetTagUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(GetTagUseCase.class);
    private final TagRepositoryPort tagRepositoryPort;

    public GetTagUseCase(TagRepositoryPort tagRepositoryPort) {
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Tag getTagByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name required");
        }
        return tagRepositoryPort.findByName(name.trim())
                .orElseThrow(() -> {
                    String msg = "Tag not found: " + name;
                    log.warn(msg);
                    return new NotFoundException(msg);
                });
    }
}
