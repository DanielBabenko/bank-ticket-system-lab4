package com.example.userservice.adapters.infrastructure.inbound;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.dto.UserRequest;
import com.example.userservice.application.usecases.UpdateUserUseCase;
import com.example.userservice.domain.ports.inbound.UpdateUserUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class UpdateUserUseCaseDecorator implements UpdateUserUseCasePort {
    private final UpdateUserUseCase delegate;

    public UpdateUserUseCaseDecorator(UpdateUserUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<UserDto> update(UUID userId, UserRequest req) {
        return delegate.update(userId, req);
    }
}
