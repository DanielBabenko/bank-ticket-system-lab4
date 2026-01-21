package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.GetApplicationUseCase;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.GetApplicationUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetApplicationUseCaseTransactionalDecorator implements GetApplicationUseCasePort {

    private final GetApplicationUseCase delegate;

    public GetApplicationUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort) {
        this.delegate = new GetApplicationUseCase(applicationRepositoryPort);
    }

    @Override
    public Optional<Application> findById(UUID id) {
        return delegate.findById(id);
    }
}
