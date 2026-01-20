package com.example.tagservice.domain.port.inbound;

import com.example.tagservice.domain.model.Tag;

/**
 * Входной порт: use-case для создания тега, если он не существует.
 * Контракты (интерфейсы) живут в доменном слое; реализации будут в application.
 */
public interface CreateTagUseCasePort {
    /**
     * Создать тег, если он ещё не существует; вернуть существующий/созданный Tag.
     *
     * @param name имя тега (обязательное, не пустое)
     * @return доменный Tag
     */
    Tag createIfNotExists(String name);
}
