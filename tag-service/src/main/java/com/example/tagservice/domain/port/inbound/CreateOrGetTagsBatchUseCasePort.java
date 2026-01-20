package com.example.tagservice.domain.port.inbound;

import com.example.tagservice.domain.model.Tag;

import java.util.List;

/**
 * Use-case для батчевого создания/получения списка тегов.
 */
public interface CreateOrGetTagsBatchUseCasePort {
    /**
     * Для списка имён: вернуть список существующих или созданных тегов.
     *
     * @param tagNames список имён (может быть пустым)
     * @return список доменных Tag (порядок не гарантируется)
     */
    List<Tag> createOrGetTags(List<String> tagNames);
}
