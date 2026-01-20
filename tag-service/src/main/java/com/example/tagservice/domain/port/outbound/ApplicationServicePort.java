package com.example.tagservice.domain.port.outbound;

import com.example.tagservice.domain.dto.ApplicationInfo;

import java.util.List;

/**
 * Порт для внешнего сервиса application-service.
 * Возвращает доменный DTO ApplicationInfo.
 */
public interface ApplicationServicePort {

    /**
     * Получить список applications, связанных с данным тегом.
     * В случае недоступности внешнего сервиса реализация может бросать exception
     * или возвращать пустой список — это решается в адаптере/слое application.
     *
     * @param tagName имя тега
     * @return список ApplicationInfo (может быть пустым, но не null в корректной реализации)
     */
    List<ApplicationInfo> getApplicationsByTag(String tagName);
}
