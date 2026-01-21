package com.example.userservice.adapters.infrastructure.inbound.transaction;

import com.example.userservice.application.usecases.DeleteUserUseCase;
import com.example.userservice.application.validator.AdminRoleValidator;
import com.example.userservice.domain.ports.UserEventPublisherPort;
import com.example.userservice.domain.ports.inbound.AdminRoleValidatorPort;
import com.example.userservice.domain.ports.inbound.DeleteUserUseCasePort;
import com.example.userservice.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import javax.xml.validation.Validator;
import java.util.UUID;

@Service
@Transactional
public class DeleteUserUseCaseTransactionalDecorator implements DeleteUserUseCasePort {
    private final DeleteUserUseCase delegate;

    public DeleteUserUseCaseTransactionalDecorator(UserRepository repo,
                                                   UserEventPublisherPort publisherPort) {
        AdminRoleValidator validator = new AdminRoleValidator(repo);
        this.delegate = new DeleteUserUseCase(repo, publisherPort, validator);
    }

    @Override
    public Mono<Void> delete(UUID userId) {
        return delegate.delete(userId);
    }
}
