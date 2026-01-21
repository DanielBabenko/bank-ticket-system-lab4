package com.example.assignmentservice.adapters.infrastructure.inbound;

import com.example.assignmentservice.application.validator.RightsValidator;
import com.example.assignmentservice.domain.ports.RightsValidatorPort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RightsValidatorDecorator implements RightsValidatorPort {
    private final RightsValidator delegate;

    public RightsValidatorDecorator(UserProductAssignmentRepository repo) {
        this.delegate = new RightsValidator(repo);
    }

    @Override
    public void checkActorRights(UUID actorId, String actorRoleClaim, UUID productId) {
        delegate.checkActorRights(actorId, actorRoleClaim, productId);
    }

    @Override
    public void checkAdminRights(UUID actorId, String actorRoleClaim) {
        delegate.checkAdminRights(actorId, actorRoleClaim);
    }
}
