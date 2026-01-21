package com.example.userservice.domain.ports.inbound;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.dto.UserRequest;
import reactor.core.publisher.Mono;

public interface CreateUserUseCasePort {
    public Mono<UserDto> create(UserRequest req);
}
