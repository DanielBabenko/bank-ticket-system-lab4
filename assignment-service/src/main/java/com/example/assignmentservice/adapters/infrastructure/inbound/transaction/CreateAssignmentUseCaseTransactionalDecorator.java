package com.example.assignmentservice.adapters.infrastructure.inbound.transaction;

import com.example.assignmentservice.application.usescases.CreateAssignmentUseCase;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.ports.CreateAssignmentUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateAssignmentUseCaseTransactionalDecorator implements CreateAssignmentUseCasePort {
    private final CreateAssignmentUseCase delegate;

    public CreateAssignmentUseCaseTransactionalDecorator(CreateAssignmentUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public UserProductAssignment assign(UUID actorId, String actorRoleClaim,
                                        UUID userId, UUID productId, AssignmentRole role) {
        return delegate.assign(actorId, actorRoleClaim, userId, productId, role);
    }
}

