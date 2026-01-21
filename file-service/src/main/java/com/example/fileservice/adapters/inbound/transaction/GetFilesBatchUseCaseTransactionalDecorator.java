package com.example.fileservice.adapters.inbound.transaction;

import com.example.fileservice.application.service.GetFilesBatchUseCase;
import com.example.fileservice.domain.port.inbound.GetFilesBatchUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetFilesBatchUseCaseTransactionalDecorator implements GetFilesBatchUseCasePort {

    private final GetFilesBatchUseCase delegate;

    public GetFilesBatchUseCaseTransactionalDecorator(FileRepositoryPort fileRepositoryPort) {
        this.delegate = new GetFilesBatchUseCase(fileRepositoryPort);
    }

    @Override
    public List<UUID> getFilesBatch(List<UUID> fileIds) {
        return delegate.getFilesBatch(fileIds);
    }
}
