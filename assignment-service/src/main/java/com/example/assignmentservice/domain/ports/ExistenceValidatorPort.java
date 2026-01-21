package com.example.assignmentservice.domain.ports;

import com.example.assignmentservice.domain.exception.NotFoundException;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;

import java.util.UUID;

public interface ExistenceValidatorPort {
    void checkUserExists(UUID userId);

    void checkProductExists(UUID productId);

    void checkUserAndProductExist(UUID userId, UUID productId);

    boolean existsByUserAndProductAndRole(UUID userId, UUID productId, AssignmentRole role);
}
