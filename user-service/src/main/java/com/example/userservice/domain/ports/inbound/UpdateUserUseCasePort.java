package com.example.userservice.domain.ports.inbound;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.dto.UserRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UpdateUserUseCasePort {
    public Mono<UserDto> update(UUID userId, UserRequest req);
}
