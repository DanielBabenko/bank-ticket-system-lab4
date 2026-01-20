package com.example.assignmentservice.application.ports;

import com.example.assignmentservice.domain.exception.ServiceUnavailableException;

import java.util.UUID;

public interface UserExistencePort {
    boolean existsById(UUID userId) throws ServiceUnavailableException;
}
