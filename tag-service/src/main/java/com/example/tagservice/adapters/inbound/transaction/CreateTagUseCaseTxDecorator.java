package com.example.tagservice.adapters.inbound.transaction;

import com.example.tagservice.application.usecase.CreateTagUseCase;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.CreateTagUseCasePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateTagUseCaseTxDecorator implements CreateTagUseCasePort {

    private final CreateTagUseCasePort delegate;

    public CreateTagUseCaseTxDecorator(TagRepositoryPort tagRepositoryPort) {
        this.delegate = new CreateTagUseCase(tagRepositoryPort);
    }

    @Override
    public Tag createIfNotExists(String name) {
        return delegate.createIfNotExists(name);
    }
}