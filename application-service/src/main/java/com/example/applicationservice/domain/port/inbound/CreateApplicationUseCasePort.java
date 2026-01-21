package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.dto.ApplicationCreateCommand;
import com.example.applicationservice.domain.model.entity.Application;

import java.util.UUID;

/**
 * Создание заявки.
 * Возвращает созданную доменную сущность.
 */
public interface CreateApplicationUseCasePort {
    Application createApplication(ApplicationCreateCommand command, UUID actorId, String actorRoleClaim);
}
