package com.example.userservice.domain.ports.inbound;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.domain.exception.BadRequestException;
import com.example.userservice.domain.exception.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FindUsersUseCasePort {
    public Flux<UserDto> findAll(int page, int size);

    public Mono<UserDto> findById(UUID id);
}
