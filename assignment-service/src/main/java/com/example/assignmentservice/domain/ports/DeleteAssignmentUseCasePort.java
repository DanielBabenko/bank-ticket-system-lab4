package com.example.assignmentservice.domain.ports;

import java.util.UUID;

public interface DeleteAssignmentUseCasePort {
    void deleteAssignments(UUID actorId, String actorRoleClaim, UUID userId, UUID productId);
}
