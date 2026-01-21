package com.example.applicationservice.domain.port.outbound;

import com.example.applicationservice.domain.event.FileEvent;
import com.example.applicationservice.domain.event.TagEvent;

/**
 * Порт для публикации событий в шину (kafka).
 */
public interface EventPublisherPort {
    void publishTagCreateRequest(TagEvent event);
    void publishTagAttachRequest(TagEvent event);
    void publishFileAttachRequest(FileEvent event);
}
