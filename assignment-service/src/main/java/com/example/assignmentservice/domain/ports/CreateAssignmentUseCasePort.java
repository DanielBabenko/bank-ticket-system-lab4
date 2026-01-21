package com.example.assignmentservice.domain.ports;

import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;

import java.util.UUID;

public interface CreateAssignmentUseCasePort {
    UserProductAssignment assign(UUID actorId, String actorRoleClaim, UUID userId, UUID productId, AssignmentRole role);
}
