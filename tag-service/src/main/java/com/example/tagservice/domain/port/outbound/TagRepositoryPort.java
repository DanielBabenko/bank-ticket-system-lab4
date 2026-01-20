package com.example.tagservice.domain.port.outbound;

import com.example.tagservice.domain.model.Tag;

import java.util.List;
import java.util.Optional;

/**
 * Порт (интерфейс) для доступа к хранилищу тегов.
 * Реализация адаптера (adapters/outbound/persistence/...) будет использовать Spring Data JPA.
 */
public interface TagRepositoryPort {

    Optional<Tag> findByName(String name);

    List<Tag> findByNames(List<String> names);

    Tag save(Tag tag);

    List<Tag> saveAll(List<Tag> tags);

    /**
     * Получить теги постранично. Реализация может использовать Pageable/Query.
     *
     * @param page номер страницы (0-based)
     * @param size размер страницы
     * @return список тегов (для данной страницы)
     */
    List<Tag> findAll(int page, int size);

    boolean existsByName(String name);
}
