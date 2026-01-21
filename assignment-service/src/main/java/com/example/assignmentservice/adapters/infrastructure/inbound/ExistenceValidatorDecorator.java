package com.example.assignmentservice.adapters.infrastructure.inbound;

import com.example.assignmentservice.application.validator.ExistenceValidator;
import com.example.assignmentservice.domain.model.enums.AssignmentRole;
import com.example.assignmentservice.domain.ports.ExistenceValidatorPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ExistenceValidatorDecorator implements ExistenceValidatorPort {
    private final ExistenceValidator delegate;

    public ExistenceValidatorDecorator(ExistenceValidator delegate) {
        this.delegate = delegate;
    }

    @Override
    public void checkUserExists(UUID userId) {
        delegate.checkUserExists(userId);
    }

    @Override
    public void checkProductExists(UUID productId) {
        delegate.checkProductExists(productId);
    }

    @Override
    public void checkUserAndProductExist(UUID userId, UUID productId) {
        delegate.checkUserAndProductExist(userId, productId);
    }

    @Override
    public boolean existsByUserAndProductAndRole(UUID userId, UUID productId, AssignmentRole role) {
        return delegate.existsByUserAndProductAndRole(userId, productId, role);
    }
}
