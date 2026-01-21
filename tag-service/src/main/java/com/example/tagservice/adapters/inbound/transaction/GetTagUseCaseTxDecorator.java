package com.example.tagservice.adapters.inbound.transaction;

import com.example.tagservice.application.usecase.GetTagUseCase;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.GetTagUseCasePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetTagUseCaseTxDecorator implements GetTagUseCasePort {

    private final GetTagUseCase delegate;

    public GetTagUseCaseTxDecorator(TagRepositoryPort tagRepositoryPort) {
        this.delegate = new GetTagUseCase(tagRepositoryPort);
    }

    @Override
    public Tag getTagByName(String name) {
        return delegate.getTagByName(name);
    }
}
