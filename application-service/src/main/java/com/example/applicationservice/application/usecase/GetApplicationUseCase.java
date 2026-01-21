package com.example.applicationservice.application.usecase;

import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.inbound.GetApplicationUseCasePort;

import java.util.Optional;
import java.util.UUID;

/**
 * Простая реализация поиска заявки.
 */
public class GetApplicationUseCase implements GetApplicationUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;

    public GetApplicationUseCase(ApplicationRepositoryPort applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Optional<Application> findById(UUID id) {
        return applicationRepository.findByIdWithFiles(id)
                .or(() -> applicationRepository.findById(id));
    }
}
