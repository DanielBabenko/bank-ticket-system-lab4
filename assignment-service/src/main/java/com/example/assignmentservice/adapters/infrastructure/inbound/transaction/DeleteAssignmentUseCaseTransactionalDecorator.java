package com.example.assignmentservice.adapters.infrastructure.inbound.transaction;

import com.example.assignmentservice.application.usescases.DeleteAssignmentUseCase;
import com.example.assignmentservice.domain.ports.DeleteAssignmentUseCasePort;
import com.example.assignmentservice.domain.ports.ExistenceValidatorPort;
import com.example.assignmentservice.domain.ports.RightsValidatorPort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteAssignmentUseCaseTransactionalDecorator implements DeleteAssignmentUseCasePort {
    private final DeleteAssignmentUseCase delegate;

    public DeleteAssignmentUseCaseTransactionalDecorator(UserProductAssignmentRepository repo,
                                                         ExistenceValidatorPort existenceValidator,
                                                         RightsValidatorPort rightsValidator) {
        this.delegate = new DeleteAssignmentUseCase(repo, existenceValidator, rightsValidator);
    }

    @Override
    public void deleteAssignments(UUID actorId, String actorRoleClaim, UUID userId, UUID productId) {
        delegate.deleteAssignments(actorId, actorRoleClaim, userId, productId);
    }
}
