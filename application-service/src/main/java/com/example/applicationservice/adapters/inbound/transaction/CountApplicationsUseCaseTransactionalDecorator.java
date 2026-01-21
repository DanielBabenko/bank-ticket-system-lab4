package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.CountApplicationsUseCase;
import com.example.applicationservice.domain.port.inbound.CountApplicationsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class CountApplicationsUseCaseTransactionalDecorator implements CountApplicationsUseCasePort {

    private final CountApplicationsUseCase delegate;

    public CountApplicationsUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort) {
        this.delegate = new CountApplicationsUseCase(applicationRepositoryPort);
    }

    @Override
    public long count() {
        return delegate.count();
    }
}
