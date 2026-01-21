package com.example.assignmentservice.domain.ports;

import com.example.assignmentservice.domain.exception.NotFoundException;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;

import java.util.UUID;

public interface ExistenceValidatorPort {
    public void checkUserExists(UUID userId);

    public void checkProductExists(UUID productId);

    public void checkUserAndProductExist(UUID userId, UUID productId);

    public boolean existsByUserAndProductAndRole(UUID userId, UUID productId, AssignmentRole role);
}
