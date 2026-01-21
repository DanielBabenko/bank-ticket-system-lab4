package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.StreamApplicationsUseCase;
import com.example.applicationservice.domain.util.ApplicationPage;
import com.example.applicationservice.domain.port.inbound.StreamApplicationsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.FileServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StreamApplicationsUseCaseTransactionalDecorator implements StreamApplicationsUseCasePort {

    private final StreamApplicationsUseCase delegate;

    public StreamApplicationsUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                           FileServicePort fileServicePort) {
        this.delegate = new StreamApplicationsUseCase(applicationRepositoryPort, fileServicePort);
    }

    @Override
    public ApplicationPage streamWithNextCursor(String cursor, int limit) {
        return delegate.streamWithNextCursor(cursor, limit);
    }
}
