package com.example.userservice.domain.ports.inbound;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChangeUserRoleUseCasePort {
    Mono<Void> promoteToManager(UUID userId);

    Mono<Void> demoteToClient(UUID userId);
}
