package com.example.userservice.application.usecases;

import com.example.userservice.domain.exception.ForbiddenException;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.exception.UnauthorizedException;
import com.example.userservice.domain.model.entity.User;
import com.example.userservice.domain.model.enums.UserRole;
import com.example.userservice.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class AdminRoleValidator {
    private static final Logger log = LoggerFactory.getLogger(AdminRoleValidator.class);

    private final UserRepository userRepository;

    public AdminRoleValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    protected Mono<User> validateAdmin() {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new UnauthorizedException("Unauthorized")))
                .flatMap(ctx -> {
                    if (ctx.getAuthentication() == null || ctx.getAuthentication().getPrincipal() == null) {
                        return Mono.error(new UnauthorizedException("Unauthorized"));
                    }
                    String principal = ctx.getAuthentication().getPrincipal().toString();
                    UUID actorId;
                    try {
                        actorId = UUID.fromString(principal);
                    } catch (IllegalArgumentException ex) {
                        return Mono.error(new UnauthorizedException("Invalid principal"));
                    }

                    return userRepository.findById(actorId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Actor not found: " + actorId)))
                            .flatMap(actor -> {
                                if (actor.getRole() != UserRole.ROLE_ADMIN) {
                                    return Mono.error(new ForbiddenException("Only ADMIN can perform this action"));
                                }
                                return Mono.just(actor);
                            });
                });
    }
}
