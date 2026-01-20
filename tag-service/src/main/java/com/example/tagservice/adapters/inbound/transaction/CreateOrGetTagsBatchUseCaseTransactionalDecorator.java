package com.example.tagservice.adapters.inbound.transaction;

import com.example.tagservice.application.usecase.CreateOrGetTagsBatchUseCase;
import com.example.tagservice.application.usecase.CreateTagUseCase;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.CreateOrGetTagsBatchUseCasePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class CreateOrGetTagsBatchUseCaseTransactionalDecorator implements CreateOrGetTagsBatchUseCasePort {

    private final CreateOrGetTagsBatchUseCase delegate;

    public CreateOrGetTagsBatchUseCaseTransactionalDecorator(TagRepositoryPort tagRepositoryPort) {
        // delegate — чистый объект application-layer, создаём его вручную
        this.delegate = new CreateOrGetTagsBatchUseCase(tagRepositoryPort);
    }

    @Override
    public List<Tag> createOrGetTags(List<String> tagNames) {
        return delegate.createOrGetTags(tagNames);
    }
}
