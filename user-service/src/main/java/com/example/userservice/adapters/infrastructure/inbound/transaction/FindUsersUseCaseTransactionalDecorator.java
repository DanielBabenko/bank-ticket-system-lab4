package com.example.userservice.adapters.infrastructure.inbound.transaction;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.application.usecases.FindUsersUseCase;
import com.example.userservice.domain.ports.inbound.FindUsersUseCasePort;
import com.example.userservice.domain.ports.inbound.UserMapperPort;
import com.example.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class FindUsersUseCaseTransactionalDecorator implements FindUsersUseCasePort {

    private final FindUsersUseCase delegate;

    public FindUsersUseCaseTransactionalDecorator(UserRepository repo, UserMapperPort mapper) {
        this.delegate = new FindUsersUseCase(repo, mapper);
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
