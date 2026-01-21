package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.DeleteApplicationsByUserIdUseCase;
import com.example.applicationservice.domain.port.inbound.DeleteApplicationsByUserIdUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteApplicationsByUserIdUseCaseTransactionalDecorator implements DeleteApplicationsByUserIdUseCasePort {

    private final DeleteApplicationsByUserIdUseCase delegate;

    public DeleteApplicationsByUserIdUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                                   ApplicationHistoryRepositoryPort historyRepositoryPort) {
        this.delegate = new DeleteApplicationsByUserIdUseCase(applicationRepositoryPort, historyRepositoryPort);
    }

    @Override
    public void deleteApplicationsByUserId(UUID userId) {
        delegate.deleteApplicationsByUserId(userId);
    }
}
