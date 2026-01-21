package com.example.userservice.domain.ports.inbound;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChangeUserRoleUseCasePort {
    public Mono<Void> promoteToManager(UUID userId);

    public Mono<Void> demoteToClient(UUID userId);
}
