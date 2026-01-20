package com.example.tagservice.domain.port.inbound;

import com.example.tagservice.domain.model.Tag;

/**
 * Use-case для чтения тега по имени.
 */
public interface GetTagUseCasePort {
    /**
     * Вернуть Tag по имени. Если не найден — бросить соответствующее исключение.
     *
     * @param name имя тега
     * @return доменный Tag
     */
    Tag getTagByName(String name);
}
