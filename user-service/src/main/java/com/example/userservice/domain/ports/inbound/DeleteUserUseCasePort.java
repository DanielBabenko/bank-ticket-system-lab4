package com.example.userservice.domain.ports.inbound;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeleteUserUseCasePort {
    public Mono<Void> delete(UUID userId);
}
