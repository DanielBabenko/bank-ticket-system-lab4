package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.DeleteApplicationsByProductIdUseCase;
import com.example.applicationservice.domain.port.inbound.DeleteApplicationsByProductIdUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteApplicationsByProductIdUseCaseTransactionalDecorator implements DeleteApplicationsByProductIdUseCasePort {

    private final DeleteApplicationsByProductIdUseCase delegate;

    public DeleteApplicationsByProductIdUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                                      ApplicationHistoryRepositoryPort historyRepositoryPort) {
        this.delegate = new DeleteApplicationsByProductIdUseCase(applicationRepositoryPort, historyRepositoryPort);
    }

    @Override
    public void deleteApplicationsByProductId(UUID productId) {
        delegate.deleteApplicationsByProductId(productId);
    }
}
