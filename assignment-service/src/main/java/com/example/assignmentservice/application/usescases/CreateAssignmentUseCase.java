package com.example.assignmentservice.application.usescases;

import com.example.assignmentservice.application.validator.ExistenceValidator;
import com.example.assignmentservice.application.validator.RightsValidator;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.ports.CreateAssignmentUseCasePort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class CreateAssignmentUseCase implements CreateAssignmentUseCasePort {
    private static final Logger logger = LoggerFactory.getLogger(CreateAssignmentUseCase.class);

    private final UserProductAssignmentRepository repo;
    private final ExistenceValidator checkExistence;
    private final RightsValidator checkRights;

    public CreateAssignmentUseCase(
            UserProductAssignmentRepository repo, ExistenceValidator checkExistence, RightsValidator checkRights) {
        this.repo = repo;
        this.checkExistence = checkExistence;
        this.checkRights = checkRights;
    }

    @Override
    public UserProductAssignment assign(UUID actorId, String actorRoleClaim, UUID userId, UUID productId, AssignmentRole role) {
        logger.info("Creating assignment: user={}, product={}, role={}, actor={} (actorRole={})",
                userId, productId, role, actorId, actorRoleClaim);

        checkRights.checkActorRights(actorId, actorRoleClaim, productId);
        checkExistence.checkUserAndProductExist(userId, productId);

        Optional<UserProductAssignment> existingAssignment = repo.findByUserIdAndProductId(userId, productId);
        UserProductAssignment assignment = new UserProductAssignment();

        if (existingAssignment.isPresent()) {
            assignment = existingAssignment.get();
            assignment.setRoleOnProduct(role);
            assignment.setAssignedAt(Instant.now());
            logger.info("Updating existing assignment: {}", assignment.getId());
        } else {
            assignment.setId(UUID.randomUUID());
            assignment.setUserId(userId);
            assignment.setProductId(productId);
            assignment.setRoleOnProduct(role);
            assignment.setAssignedAt(Instant.now());
            logger.info("Creating new assignment");
        }

        return repo.save(assignment);
    }
}
