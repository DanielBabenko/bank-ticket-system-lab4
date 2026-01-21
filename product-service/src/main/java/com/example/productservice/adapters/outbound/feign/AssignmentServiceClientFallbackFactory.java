package com.example.productservice.adapters.outbound.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AssignmentServiceClientFallbackFactory implements FallbackFactory<AssignmentServiceClientFeign> {
    @Override
    public AssignmentServiceClientFeign create(Throwable cause) {
        return new AssignmentServiceClientFeign() {
            @Override
            public Boolean existsByUserAndProductAndRole(UUID userId, UUID productId, String role) {
                return null;
            }
        };
    }
}
