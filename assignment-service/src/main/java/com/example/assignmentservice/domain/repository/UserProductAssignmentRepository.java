package com.example.assignmentservice.domain.repository;

import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProductAssignmentRepository{
    List<UserProductAssignment> findByUserId(UUID userId);
    List<UserProductAssignment> findByProductId(UUID productId);
    Optional<UserProductAssignment> findByUserIdAndProductId(UUID userId, UUID productId);
    boolean existsByUserIdAndProductIdAndRoleOnProduct(UUID userId, UUID productId, AssignmentRole role);
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
    void deleteByUserId(UUID userId);
    void deleteByProductId(UUID productId);
    UserProductAssignment save(UserProductAssignment assignment);
    void deleteAll();
    List<UserProductAssignment> findAll();
}
