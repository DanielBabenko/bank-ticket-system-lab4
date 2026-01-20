package com.example.assignmentservice.application.usescases;

import com.example.assignmentservice.domain.exception.NotFoundException;
import com.example.assignmentservice.domain.exception.ServiceUnavailableException;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import com.example.assignmentservice.infrastructure.feign.ProductServiceClient;
import com.example.assignmentservice.infrastructure.feign.UserServiceClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class CheckExistence {
    private static final Logger logger = LoggerFactory.getLogger(CheckExistence.class);

    private final UserProductAssignmentRepository repo;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    public CheckExistence(UserProductAssignmentRepository repo, UserServiceClient userServiceClient, ProductServiceClient productServiceClient) {
        this.repo = repo;
        this.userServiceClient = userServiceClient;
        this.productServiceClient = productServiceClient;
    }

    protected void checkUserExists(UUID userId) {
        try {
            Boolean exists = userServiceClient.userExists(userId);
            if (exists == null) {
                logger.warn("Поймал null");
                throw new ServiceUnavailableException("Cannot verify user. User service unavailable now");
            }
            if (!exists) {
                logger.warn("Поймал !exists");
                throw new NotFoundException("User not found: " + userId);
            }
        } catch (FeignException e) {
            logger.warn("Поймал только FeignException: {}", e.status());
            logger.error("Error checking user existence: {}", e.getMessage());
            throw new ServiceUnavailableException("Cannot verify user. User service is unavailable now");
        }
    }

    protected void checkProductExists(UUID productId) {
        try {
            Boolean exists = productServiceClient.productExists(productId);
            if (exists == null) {
                throw new ServiceUnavailableException("Cannot verify product. Product service unavailable now");
            }
            if (!exists) {
                throw new NotFoundException("Product not found: " + productId);
            }
        } catch (FeignException e) {
            logger.error("Error checking product existence: {}", e.getMessage());
            throw new ServiceUnavailableException("Cannot verify product. Product service is unavailable now");
        }
    }

    protected void checkUserAndProductExist(UUID userId, UUID productId) {
        checkUserExists(userId);
        checkProductExists(productId);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserAndProductAndRole(UUID userId, UUID productId, AssignmentRole role) {
        return repo.existsByUserIdAndProductIdAndRoleOnProduct(userId, productId, role);
    }
}
