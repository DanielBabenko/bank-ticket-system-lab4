package com.example.productservice.domain.port.outbound;

import java.util.UUID;

public interface AssignmentServicePort {

    Boolean hasRole(UUID userId, UUID productId, String role);
}
