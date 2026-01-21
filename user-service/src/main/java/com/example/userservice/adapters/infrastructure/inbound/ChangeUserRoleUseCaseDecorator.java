package com.example.userservice.adapters.infrastructure.inbound;

import com.example.userservice.application.usecases.ChangeUserRoleUseCase;
import com.example.userservice.domain.ports.inbound.ChangeUserRoleUseCasePort;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ChangeUserRoleUseCaseDecorator implements ChangeUserRoleUseCasePort {
    private final ChangeUserRoleUseCase delegate;

    public ChangeUserRoleUseCaseDecorator(ChangeUserRoleUseCase delegate) {
        this.delegate = delegate;
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
