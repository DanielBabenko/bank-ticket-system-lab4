package com.example.userservice.domain.ports.inbound;

import com.example.userservice.domain.model.entity.User;
import reactor.core.publisher.Mono;

public interface AdminRoleValidatorPort {
    Mono<User> validateAdmin();
}
