package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.FindApplicationsByTagUseCase;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.port.inbound.FindApplicationsByTagUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FindApplicationsByTagUseCaseTransactionalDecorator implements FindApplicationsByTagUseCasePort {

    private final FindApplicationsByTagUseCase delegate;

    public FindApplicationsByTagUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort) {
        this.delegate = new FindApplicationsByTagUseCase(applicationRepositoryPort);
    }

    @Override
    public List<ApplicationInfo> findApplicationsByTag(String tagName) {
        return delegate.findApplicationsByTag(tagName);
    }
}
