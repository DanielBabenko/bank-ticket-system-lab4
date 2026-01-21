package com.example.applicationservice.domain.port.outbound;

import java.util.UUID;

/**
 * Порт для user-service.
 * Возвращаем Boolean: true/false при успехе, null или исключение — на недоступность.
 */
public interface UserServicePort {
    Boolean userExists(UUID userId);
}
