package com.example.fileservice.adapters.inbound.transaction;

import com.example.fileservice.application.command.UploadFileCommand;
import com.example.fileservice.application.usecase.UploadFileUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.UploadFileUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UploadFileUseCaseTransactionalDecorator implements UploadFileUseCasePort {

    private final UploadFileUseCase delegate;

    public UploadFileUseCaseTransactionalDecorator(FileRepositoryPort fileRepositoryPort, StoragePort storagePort) {
        this.delegate = new UploadFileUseCase(fileRepositoryPort, storagePort);
    }

    @Override
    public File uploadFile(UploadFileCommand command) {
        return delegate.uploadFile(command);
    }
}
