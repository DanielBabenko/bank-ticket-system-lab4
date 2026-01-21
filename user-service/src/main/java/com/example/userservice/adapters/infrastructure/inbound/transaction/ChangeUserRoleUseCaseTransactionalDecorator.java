package com.example.userservice.adapters.infrastructure.inbound.transaction;

import com.example.userservice.application.usecases.ChangeUserRoleUseCase;
import com.example.userservice.application.validator.AdminRoleValidator;
import com.example.userservice.domain.ports.inbound.AdminRoleValidatorPort;
import com.example.userservice.domain.ports.inbound.ChangeUserRoleUseCasePort;
import com.example.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class ChangeUserRoleUseCaseTransactionalDecorator implements ChangeUserRoleUseCasePort {
    private final ChangeUserRoleUseCase delegate;

    public ChangeUserRoleUseCaseTransactionalDecorator(UserRepository repo) {
        AdminRoleValidator validator = new AdminRoleValidator(repo);
        this.delegate = new ChangeUserRoleUseCase(repo, validator);
    }

    @Override
    public Mono<Void> promoteToManager(UUID userId) {
        return delegate.promoteToManager(userId);
    }

    @Override
    public Mono<Void> demoteToClient(UUID userId) {
        return delegate.demoteToClient(userId);
    }
}
