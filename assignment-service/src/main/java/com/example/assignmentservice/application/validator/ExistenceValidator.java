package com.example.assignmentservice.application.validator;

import com.example.assignmentservice.application.ports.ProductExistencePort;
import com.example.assignmentservice.application.ports.UserExistencePort;
import com.example.assignmentservice.domain.exception.NotFoundException;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.ports.ExistenceValidatorPort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ExistenceValidator implements ExistenceValidatorPort {
    private static final Logger logger = LoggerFactory.getLogger(ExistenceValidator.class);

    private final UserProductAssignmentRepository repo;
    private final UserExistencePort userExistencePort;
    private final ProductExistencePort productExistencePort;

    public ExistenceValidator(UserProductAssignmentRepository repo,
                              UserExistencePort userExistencePort, ProductExistencePort productExistencePort) {
        this.repo = repo;
        this.userExistencePort = userExistencePort;
        this.productExistencePort = productExistencePort;
    }

    @Override
    public void checkUserExists(UUID userId) {
        if (!userExistencePort.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    @Override
    public void checkProductExists(UUID productId) {
        if (!productExistencePort.existsById(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
    }

    @Override
    public void checkUserAndProductExist(UUID userId, UUID productId) {
        checkUserExists(userId);
        checkProductExists(productId);
    }

    @Override
    public boolean existsByUserAndProductAndRole(UUID userId, UUID productId, AssignmentRole role) {
        return repo.existsByUserIdAndProductIdAndRoleOnProduct(userId, productId, role);
    }
}
