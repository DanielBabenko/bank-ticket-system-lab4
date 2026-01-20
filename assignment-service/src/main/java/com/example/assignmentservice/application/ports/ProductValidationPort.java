package com.example.assignmentservice.application.ports;

import java.util.UUID;

public interface ProductValidationPort {
    void validateProductExists(UUID productId);
}
