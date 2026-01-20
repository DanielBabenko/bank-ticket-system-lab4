package com.example.userservice.domain.ports;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface UserEventPublisherPort {
    Mono<Void> publishUserDeletedEvent(UUID userId);
}

