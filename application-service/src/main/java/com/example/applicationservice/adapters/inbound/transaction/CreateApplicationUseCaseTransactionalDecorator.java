package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.CreateApplicationUseCase;
import com.example.applicationservice.domain.dto.ApplicationCreateCommand;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.CreateApplicationUseCasePort;
import com.example.applicationservice.domain.port.outbound.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Transactional decorator for CreateApplicationUseCasePort.
 * Delegates to application.usecase (POJO) while providing @Transactional boundary.
 */
@Service
@Transactional
public class CreateApplicationUseCaseTransactionalDecorator implements CreateApplicationUseCasePort {

    private final CreateApplicationUseCase delegate;

    public CreateApplicationUseCaseTransactionalDecorator(
            ApplicationRepositoryPort applicationRepositoryPort,
            ApplicationHistoryRepositoryPort applicationHistoryRepositoryPort,
            UserServicePort userServicePort,
            ProductServicePort productServicePort,
            FileServicePort fileServicePort,
            EventPublisherPort eventPublisherPort
    ) {
        this.delegate = new CreateApplicationUseCase(
                applicationRepositoryPort,
                applicationHistoryRepositoryPort,
                userServicePort,
                productServicePort,
                fileServicePort,
                eventPublisherPort
        );
    }

    @Override
    public Application createApplication(ApplicationCreateCommand command, UUID actorId, String actorRoleClaim) {
        return delegate.createApplication(command, actorId, actorRoleClaim);
    }
}
