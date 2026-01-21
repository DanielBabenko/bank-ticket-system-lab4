package com.example.assignmentservice.adapters.infrastructure.inbound;

import com.example.assignmentservice.application.validator.RightsValidator;
import com.example.assignmentservice.domain.ports.RightsValidatorPort;

import java.util.UUID;

public class RightsValidatorDecorator implements RightsValidatorPort {
    private final RightsValidator delegate;

    public RightsValidatorDecorator(RightsValidator delegate) {
        this.delegate = delegate;
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
