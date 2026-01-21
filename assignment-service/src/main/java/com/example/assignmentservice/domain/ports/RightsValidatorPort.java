package com.example.assignmentservice.domain.ports;

import java.util.UUID;

public interface RightsValidatorPort {
    void checkActorRights(UUID actorId, String actorRoleClaim, UUID productId);
    void checkAdminRights(UUID actorId, String actorRoleClaim);
}
