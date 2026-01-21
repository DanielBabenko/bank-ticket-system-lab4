package com.example.assignmentservice.adapters.infrastructure.inbound.transaction;

import com.example.assignmentservice.application.usescases.DeleteAssignmentUseCase;
import com.example.assignmentservice.domain.ports.DeleteAssignmentUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteAssignmentUseCaseTransactionalDecorator implements DeleteAssignmentUseCasePort {
    private final DeleteAssignmentUseCase delegate;

    public DeleteAssignmentUseCaseTransactionalDecorator(DeleteAssignmentUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public void deleteAssignments(UUID actorId, String actorRoleClaim, UUID userId, UUID productId) {
        delegate.deleteAssignments(actorId, actorRoleClaim, userId, productId);
    }
}
