package com.example.productservice.adapters.outbound.feign;

import com.example.productservice.domain.port.outbound.AssignmentServicePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AssignmentServiceAdapter implements AssignmentServicePort {

    private final AssignmentServiceClientFeign client;

    public AssignmentServiceAdapter(AssignmentServiceClientFeign client) {
        this.client = client;
    }

    @Override
    public Boolean hasRole(UUID userId, UUID productId, String role) {
        return client.existsByUserAndProductAndRole(userId, productId, role);
    }
}
