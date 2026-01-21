package com.example.fileservice.adapters.inbound.transaction;

import com.example.fileservice.application.usecase.DeleteFileUseCase;
import com.example.fileservice.domain.port.inbound.DeleteFileUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteFileUseCaseTransactionalDecorator implements DeleteFileUseCasePort {

    private final DeleteFileUseCase delegate;

    public DeleteFileUseCaseTransactionalDecorator(FileRepositoryPort fileRepositoryPort, StoragePort storagePort) {
        this.delegate = new DeleteFileUseCase(fileRepositoryPort, storagePort);
    }

    @Override
    public void deleteFile(UUID fileId, UUID userId, boolean isAdmin) {
        delegate.deleteFile(fileId, userId, isAdmin);
    }
}
