package com.example.assignmentservice.application.ports;

import com.example.assignmentservice.domain.exception.ServiceUnavailableException;

import java.util.UUID;

public interface ProductExistencePort {
    boolean existsById(UUID productId) throws ServiceUnavailableException;
}
