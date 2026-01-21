package com.example.applicationservice.application.usecase;

import com.example.applicationservice.domain.port.inbound.CountApplicationsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

/**
 * Simple count use-case.
 */
public class CountApplicationsUseCase implements CountApplicationsUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;

    public CountApplicationsUseCase(ApplicationRepositoryPort applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public long count() {
        return applicationRepository.count();
    }
}
