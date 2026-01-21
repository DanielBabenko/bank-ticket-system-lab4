package com.example.assignmentservice.adapters.infrastructure.client.adapter;

import com.example.assignmentservice.application.ports.UserExistencePort;
import com.example.assignmentservice.domain.exception.ServiceUnavailableException;
import com.example.assignmentservice.adapters.infrastructure.client.feign.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserExistenceAdapter implements UserExistencePort {
    private static final Logger log = LoggerFactory.getLogger(UserExistenceAdapter.class);
    private final UserServiceClient userServiceClient;

    public UserExistenceAdapter(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean existsById(UUID userId) {
        try {
            Boolean exists = userServiceClient.userExists(userId);
            if (exists == null) {
                log.warn("User service returned null for user existence check: {}", userId);
                throw new ServiceUnavailableException("User service unavailable");
            }
            return exists;
        } catch (Exception e) {
            log.error("Error checking user existence: {}", e.getMessage());
            throw new ServiceUnavailableException("User service is unavailable");
        }
    }
}
