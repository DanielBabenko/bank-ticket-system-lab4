package com.example.userservice.adapters.infrastructure.inbound.transaction;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.dto.UserRequest;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.application.usecases.CreateUserUseCase;
import com.example.userservice.domain.ports.inbound.CreateUserUseCasePort;
import com.example.userservice.domain.ports.inbound.UserMapperPort;
import com.example.userservice.domain.repository.UserRepository;
import org.apache.kafka.common.config.types.Password;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class CreateUserUseCaseTransactionalDecorator implements CreateUserUseCasePort {
    private final CreateUserUseCase delegate;

    public CreateUserUseCaseTransactionalDecorator(UserRepository repo,
                                                   PasswordEncoder passwordEncoder) {
        UserMapper mapper = new UserMapper();
        this.delegate = new CreateUserUseCase(repo, passwordEncoder, mapper);
    }

    @Override
    public Mono<UserDto> create(UserRequest req) {
        return delegate.create(req);
    }
}
