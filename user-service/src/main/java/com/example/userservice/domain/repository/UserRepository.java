package com.example.userservice.domain.repository;

import com.example.userservice.domain.model.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public interface UserRepository {
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);

    Mono<User> findById(UUID actorId);
    Mono<User> save(User user);
    Mono<Void> delete(User user);
    Flux<User> findAll();
}
