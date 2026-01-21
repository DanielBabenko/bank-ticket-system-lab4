package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.model.entity.Application;

import java.util.Optional;
import java.util.UUID;

public interface GetApplicationUseCasePort {
    Optional<Application> findById(UUID id);
}
