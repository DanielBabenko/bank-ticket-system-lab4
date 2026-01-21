package com.example.assignmentservice.adapters.infrastructure.inbound.transaction;

import com.example.assignmentservice.application.usescases.CreateAssignmentUseCase;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.ports.CreateAssignmentUseCasePort;
import com.example.assignmentservice.domain.ports.ExistenceValidatorPort;
import com.example.assignmentservice.domain.ports.RightsValidatorPort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateAssignmentUseCaseTransactionalDecorator implements CreateAssignmentUseCasePort {
    private final CreateAssignmentUseCase delegate;

    public CreateAssignmentUseCaseTransactionalDecorator(UserProductAssignmentRepository repo,
                                                         ExistenceValidatorPort existenceValidator,
                                                         RightsValidatorPort rightsValidator) {
        this.delegate = new CreateAssignmentUseCase(repo, existenceValidator, rightsValidator);
    }

    @Override
    public UserProductAssignment assign(UUID actorId, String actorRoleClaim,
                                        UUID userId, UUID productId, AssignmentRole role) {
        return delegate.assign(actorId, actorRoleClaim, userId, productId, role);
    }
}

