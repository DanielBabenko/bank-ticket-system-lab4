package com.example.assignmentservice.application.usescases;

import com.example.assignmentservice.domain.exception.UnauthorizedException;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteAssignmentUseCase {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAssignmentUseCase.class);

    private final UserProductAssignmentRepository repo;
    private final ExistenceValidator checkExistence;
    private final RightsValidator checkRights;

    public DeleteAssignmentUseCase(
            UserProductAssignmentRepository repo, ExistenceValidator checkExistence, RightsValidator checkRights) {
        this.repo = repo;
        this.checkExistence = checkExistence;
        this.checkRights = checkRights;
    }

    @Transactional
    public void deleteAssignments(UUID actorId, String actorRoleClaim, UUID userId, UUID productId) {
        logger.info("Deleting assignments: actor={}, actorRole={}, user={}, product={}",
                actorId, actorRoleClaim, userId, productId);

        if (actorId == null) {
            throw new UnauthorizedException("Actor ID is required");
        }

        checkRights.checkAdminRights(actorId, actorRoleClaim);

        if (userId != null && productId != null) {
            checkExistence.checkUserAndProductExist(userId, productId);
            repo.deleteByUserIdAndProductId(userId, productId);
            logger.info("Deleted assignment for user {} and product {}", userId, productId);

        } else if (userId != null) {
            checkExistence.checkUserExists(userId);
            repo.deleteByUserId(userId);
            logger.info("Deleted all assignments for user {}", userId);

        } else if (productId != null) {
            checkExistence.checkProductExists(productId);
            repo.deleteByProductId(productId);
            logger.info("Deleted all assignments for product {}", productId);

        } else {
            repo.deleteAll();
            logger.info("Deleted all assignments");
        }
    }

    @Transactional
    public void deleteByProductId(UUID productId) {
        repo.deleteByProductId(productId);
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        repo.deleteByUserId(userId);
    }
}
