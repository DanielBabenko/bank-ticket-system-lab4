package com.example.userservice.adapters.presentation.controller;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.usecases.*;
import com.example.userservice.application.dto.UserRequest;
import com.example.userservice.domain.model.enums.UserRole;
import com.example.userservice.domain.ports.inbound.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Users", description = "API for managing users")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserUseCasePort createUseCasePort;
    private final FindUsersUseCasePort findUseCasePort;
    private final DeleteUserUseCasePort deleteUseCasePort;
    private final UpdateUserUseCasePort updateUseCasePort;
    private final ChangeUserRoleUseCasePort changeUserRoleUseCasePort;

    private static final int MAX_PAGE_SIZE = 50;

    public UserController(CreateUserUseCasePort createUseCasePort, FindUsersUseCasePort findUseCasePort, DeleteUserUseCasePort deleteUseCasePort, UpdateUserUseCasePort updateUseCasePort, ChangeUserRoleUseCasePort changeUserRoleUseCasePort) {
        this.createUseCasePort = createUseCasePort;
        this.findUseCasePort = findUseCasePort;
        this.deleteUseCasePort = deleteUseCasePort;
        this.updateUseCasePort = updateUseCasePort;
        this.changeUserRoleUseCasePort = changeUserRoleUseCasePort;
    }


    // Создавать пользователей может только ADMIN (SUPERVISOR)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<UserDto> createUser(@Valid @RequestBody UserRequest request) {
        return createUseCasePort.create(request);
    }

    @GetMapping
    public Flux<UserDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (size > MAX_PAGE_SIZE) {
            return Flux.error(new IllegalArgumentException(
                    String.format("Page size cannot be greater than %d", MAX_PAGE_SIZE)));
        }
        return findUseCasePort.findAll(page, size);
    }

    @GetMapping("/{id}")
    public Mono<UserDto> getUserById(@PathVariable UUID id) {
        return findUseCasePort.findById(id);
    }

    // Обновление пользователя — только ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<UserDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequest request) {
        return updateUseCasePort.update(id, request);
    }

    // Удаление пользователя — только ADMIN
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<Void> deleteUser(@PathVariable UUID id) {
        return deleteUseCasePort.delete(id);
    }

    @PutMapping("/{id}/promote-manager")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<Void> promoteToManager(@PathVariable UUID id) {
        return changeUserRoleUseCasePort.promoteToManager(id);
    }

    @PutMapping("/{id}/demote-manager")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<Void> demoteToClient(@PathVariable UUID id) {
        return changeUserRoleUseCasePort.demoteToClient(id);
    }

    @GetMapping("/{id}/exists")
    public Mono<ResponseEntity<Boolean>> userExists(@PathVariable UUID id) {
        return findUseCasePort.findById(id)
                .map(user -> ResponseEntity.ok(true))
                .defaultIfEmpty(ResponseEntity.ok(false));
    }

    @GetMapping("/{id}/role")
    public Mono<ResponseEntity<UserRole>> getUserRole(@PathVariable UUID id) {
        return findUseCasePort.findById(id)
                .map(user -> ResponseEntity.ok(user.getRole()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}