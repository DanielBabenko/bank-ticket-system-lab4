package com.example.userservice.infrastructure.persistence;

import com.example.userservice.domain.model.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserR2dbcRepository extends R2dbcRepository<User, UUID> {
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);

    Mono<User> findById(UUID actorId);
    Mono<User> save(User user);
    Mono<Void> delete(User user);
    Flux<User> findAll();
}
