package com.example.assignmentservice.application.ports;

import java.util.UUID;

public interface UserValidationPort {
    void validateUserExists(UUID userId);
}
