package com.example.assignmentservice.application.validator;

import com.example.assignmentservice.domain.exception.ForbiddenException;
import com.example.assignmentservice.domain.exception.NotFoundException;
import com.example.assignmentservice.domain.exception.ServiceUnavailableException;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.ports.RightsValidatorPort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class RightsValidator implements RightsValidatorPort {
    private static final Logger logger = LoggerFactory.getLogger(RightsValidator.class);

    private final UserProductAssignmentRepository repo;

    public RightsValidator(
            UserProductAssignmentRepository repo) {
        this.repo = repo;
    }

    @Override
    public void checkActorRights(UUID actorId, String actorRoleClaim, UUID productId) {
        try {
            boolean isAdmin = false;
            if (actorRoleClaim != null) {
                isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
            }

            boolean isOwner = repo.existsByUserIdAndProductIdAndRoleOnProduct(
                    actorId, productId, AssignmentRole.PRODUCT_OWNER);

            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("Only ADMIN or PRODUCT_OWNER can assign products");
            }
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Actor not found: " + actorId);
        } catch (FeignException | ServiceUnavailableException e) {
            logger.error("Error checking actor rights: {}", e.getMessage());
            throw new ServiceUnavailableException("Cannot verify user rights. User service is unavailable now");
        }
    }

    @Override
    public void checkAdminRights(UUID actorId, String actorRoleClaim) {
        try {
            boolean isAdmin = false;
            if (actorRoleClaim != null) {
                isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
            }
            if (!isAdmin) {
                throw new ForbiddenException("Only ADMIN can delete assignments");
            }
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Actor not found: " + actorId);
        } catch (FeignException | ServiceUnavailableException e) {
            logger.error("Error checking admin rights: {}", e.getMessage());
            throw new ServiceUnavailableException("Cannot verify admin rights. User service is unavailable now");
        }
    }
}
