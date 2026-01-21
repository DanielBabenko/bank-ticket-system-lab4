package com.example.userservice.adapters.infrastructure.inbound;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.dto.UserRequest;
import com.example.userservice.application.usecases.CreateUserUseCase;
import com.example.userservice.domain.ports.inbound.CreateUserUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class CreateUserUseCaseDecorator implements CreateUserUseCasePort {
    private final CreateUserUseCase delegate;

    public CreateUserUseCaseDecorator(CreateUserUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<UserDto> create(UserRequest req) {
        return delegate.create(req);
    }
}
