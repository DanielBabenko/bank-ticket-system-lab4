package com.example.userservice.adapters.infrastructure.inbound.transaction;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.usecases.FindUsersUseCase;
import com.example.userservice.domain.ports.inbound.FindUsersUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class FindUsersUseCaseTransactionalDecorator implements FindUsersUseCasePort {

    private final FindUsersUseCase delegate;

    public FindUsersUseCaseTransactionalDecorator(FindUsersUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Flux<UserDto> findAll(int page, int size) {
        return delegate.findAll(page, size);
    }

    @Override
    public Mono<UserDto> findById(UUID id) {
        return delegate.findById(id);
    }
}
