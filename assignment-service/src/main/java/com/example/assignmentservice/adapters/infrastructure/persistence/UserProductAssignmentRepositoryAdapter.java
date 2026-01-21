package com.example.assignmentservice.adapters.infrastructure.persistence;

import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserProductAssignmentRepositoryAdapter implements UserProductAssignmentRepository {

    private final UserProductAssignmentJpaRepository jpaRepository;

    public UserProductAssignmentRepositoryAdapter(UserProductAssignmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<UserProductAssignment> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<UserProductAssignment> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public Optional<UserProductAssignment> findByUserIdAndProductId(UUID userId, UUID productId) {
        return jpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean existsByUserIdAndProductIdAndRoleOnProduct(UUID userId, UUID productId, AssignmentRole role) {
        return jpaRepository.existsByUserIdAndProductIdAndRoleOnProduct(userId, productId, role);
    }

    @Override
    public boolean existsByUserIdAndProductId(UUID userId, UUID productId) {
        return jpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public void deleteByUserIdAndProductId(UUID userId, UUID productId) {
        jpaRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteByProductId(UUID productId) {
        jpaRepository.deleteByProductId(productId);
    }

    @Override
    public UserProductAssignment save(UserProductAssignment assignment) {
        return jpaRepository.save(assignment);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public List<UserProductAssignment> findAll() {
        return jpaRepository.findAll();
    }
}
