package com.example.assignmentservice.adapters.infrastructure.inbound;

import com.example.assignmentservice.application.usescases.DeleteAssignmentUseCase;
import com.example.assignmentservice.domain.ports.DeleteAssignmentUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteAssignmentUseCaseDecorator implements DeleteAssignmentUseCasePort {
    private final DeleteAssignmentUseCase delegate;

    public DeleteAssignmentUseCaseDecorator(DeleteAssignmentUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public void deleteAssignments(UUID actorId, String actorRoleClaim, UUID userId, UUID productId) {
        delegate.deleteAssignments(actorId, actorRoleClaim, userId, productId);
    }
}
