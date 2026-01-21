package com.example.userservice.adapters.infrastructure.inbound;

import com.example.userservice.application.usecases.DeleteUserUseCase;
import com.example.userservice.domain.ports.inbound.DeleteUserUseCasePort;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class DeleteUserUseCaseDecorator implements DeleteUserUseCasePort {
    private final DeleteUserUseCase delegate;

    public DeleteUserUseCaseDecorator(DeleteUserUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<Void> delete(UUID userId) {
        return delegate.delete(userId);
    }
}
