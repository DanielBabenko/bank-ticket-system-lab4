package com.example.assignmentservice.adapters.infrastructure.inbound;

import com.example.assignmentservice.application.usescases.CreateAssignmentUseCase;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.ports.CreateAssignmentUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CreateAssignmentUseCaseDecorator implements CreateAssignmentUseCasePort {
    private final CreateAssignmentUseCase delegate;

    public CreateAssignmentUseCaseDecorator(CreateAssignmentUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public UserProductAssignment assign(UUID actorId, String actorRoleClaim,
                                        UUID userId, UUID productId, AssignmentRole role) {
        return delegate.assign(actorId, actorRoleClaim, userId, productId, role);
    }
}

