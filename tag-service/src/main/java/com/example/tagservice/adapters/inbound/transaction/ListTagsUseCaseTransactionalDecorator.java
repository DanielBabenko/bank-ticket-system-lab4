package com.example.tagservice.adapters.inbound.transaction;

import com.example.tagservice.application.usecase.ListTagsUseCase;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.ListTagsUseCasePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListTagsUseCaseTransactionalDecorator implements ListTagsUseCasePort {

    private final ListTagsUseCase delegate;

    public ListTagsUseCaseTransactionalDecorator(TagRepositoryPort tagRepositoryPort) {
        this.delegate = new ListTagsUseCase(tagRepositoryPort);
    }

    @Override
    public List<Tag> listAll(int page, int size) {
        return delegate.listAll(page, size);
    }
}
