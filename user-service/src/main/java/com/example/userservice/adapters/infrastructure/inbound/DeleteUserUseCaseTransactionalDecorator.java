package com.example.userservice.adapters.infrastructure.inbound;

import com.example.userservice.application.usecases.DeleteUserUseCase;
import com.example.userservice.domain.ports.inbound.DeleteUserUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class DeleteUserUseCaseTransactionalDecorator implements DeleteUserUseCasePort {
    private final DeleteUserUseCase delegate;

    public DeleteUserUseCaseTransactionalDecorator(DeleteUserUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<Void> delete(UUID userId) {
        return delegate.delete(userId);
    }
}
