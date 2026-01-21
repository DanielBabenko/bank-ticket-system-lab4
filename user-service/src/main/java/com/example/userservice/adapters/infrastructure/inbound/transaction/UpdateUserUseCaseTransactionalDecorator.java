package com.example.userservice.adapters.infrastructure.inbound.transaction;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.dto.UserRequest;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.application.usecases.UpdateUserUseCase;
import com.example.userservice.application.validator.AdminRoleValidator;
import com.example.userservice.domain.ports.UserEventPublisherPort;
import com.example.userservice.domain.ports.inbound.AdminRoleValidatorPort;
import com.example.userservice.domain.ports.inbound.UpdateUserUseCasePort;
import com.example.userservice.domain.ports.inbound.UserMapperPort;
import com.example.userservice.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class UpdateUserUseCaseTransactionalDecorator implements UpdateUserUseCasePort {
    private final UpdateUserUseCase delegate;

    public UpdateUserUseCaseTransactionalDecorator(UserRepository repo,
                                                   PasswordEncoder passwordEncoder) {
        AdminRoleValidator validator = new AdminRoleValidator(repo);
        UserMapper mapper = new UserMapper();
        this.delegate = new UpdateUserUseCase(repo, passwordEncoder, validator, mapper);
    }

    @Override
    public Mono<UserDto> update(UUID userId, UserRequest req) {
        return delegate.update(userId, req);
    }
}
