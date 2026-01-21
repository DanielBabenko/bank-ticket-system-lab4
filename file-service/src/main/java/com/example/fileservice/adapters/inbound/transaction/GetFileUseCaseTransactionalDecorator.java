package com.example.fileservice.adapters.inbound.transaction;

import com.example.fileservice.application.service.GetFileUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.GetFileUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetFileUseCaseTransactionalDecorator implements GetFileUseCasePort {

    private final GetFileUseCase delegate;

    public GetFileUseCaseTransactionalDecorator(FileRepositoryPort fileRepositoryPort) {
        this.delegate = new GetFileUseCase(fileRepositoryPort);
    }

    @Override
    public File getFileById(UUID id) {
        return delegate.getFileById(id);
    }
}
