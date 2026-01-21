package com.example.assignmentservice.domain.ports;

import java.util.UUID;

public interface RightsValidatorPort {
    public void checkActorRights(UUID actorId, String actorRoleClaim, UUID productId);
    public void checkAdminRights(UUID actorId, String actorRoleClaim);
}
