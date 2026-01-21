package com.example.userservice.domain.ports.inbound;

import com.example.userservice.application.dto.UserDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FindUsersUseCasePort {
    Flux<UserDto> findAll(int page, int size);

    Mono<UserDto> findById(UUID id);
}
