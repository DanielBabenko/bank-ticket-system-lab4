package com.example.applicationservice.adapters.outbound.feign;

import com.example.applicationservice.domain.port.outbound.UserServicePort;
import com.example.applicationservice.adapters.outbound.feign.client.UserServiceClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter for user-service Feign client.
 * Returns Boolean or throws exception if service unreachable (Feign fallback factory handles throwing ServiceUnavailableException).
 */
@Component
public class UserServiceAdapter implements UserServicePort {

    private final UserServiceClient client;

    public UserServiceAdapter(UserServiceClient client) {
        this.client = client;
    }

    @Override
    public Boolean userExists(UUID userId) {
        return client.userExists(userId);
    }
}
