package com.example.fileservice.adapters.inbound.transaction;

import com.example.fileservice.application.service.DownloadFileUseCase;
import com.example.fileservice.domain.port.inbound.DownloadFileUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DownloadFileUseCaseTransactionalDecorator implements DownloadFileUseCasePort {

    private final DownloadFileUseCase delegate;

    public DownloadFileUseCaseTransactionalDecorator(
            FileRepositoryPort fileRepositoryPort,
            StoragePort storagePort
    ) {
        this.delegate = new DownloadFileUseCase(fileRepositoryPort, storagePort);
    }

    @Override
    public DownloadResult downloadFile(UUID fileId, UUID userId, Object jwt) {
        return delegate.downloadFile(fileId, userId, jwt);
    }
}
