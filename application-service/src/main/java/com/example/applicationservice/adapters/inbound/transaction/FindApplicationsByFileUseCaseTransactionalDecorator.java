package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.FindApplicationsByFileUseCase;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.port.inbound.FindApplicationsByFileUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FindApplicationsByFileUseCaseTransactionalDecorator implements FindApplicationsByFileUseCasePort {

    private final FindApplicationsByFileUseCase delegate;

    public FindApplicationsByFileUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort) {
        this.delegate = new FindApplicationsByFileUseCase(applicationRepositoryPort);
    }

    @Override
    public List<ApplicationInfo> findApplicationsByFile(UUID fileId) {
        return delegate.findApplicationsByFile(fileId);
    }
}
