package com.example.userservice.infrastructure.persistence;

import com.example.userservice.domain.model.entity.User;
import com.example.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserR2dbcRepository r2dbcRepository;

    public UserRepositoryAdapter(UserR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return r2dbcRepository.existsByUsername(username);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return r2dbcRepository.existsByEmail(email);
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return r2dbcRepository.findByUsername(username);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return r2dbcRepository.findByEmail(email);
    }

    @Override
    public Mono<User> findById(UUID actorId) {
        return r2dbcRepository.findById(actorId);
    }

    @Override
    public Mono<User> save(User user) {
        return r2dbcRepository.save(user);
    }

    @Override
    public Mono<Void> delete(User user) {
        return r2dbcRepository.delete(user);
    }

    @Override
    public Flux<User> findAll() {
        return r2dbcRepository.findAll();
    }
}
