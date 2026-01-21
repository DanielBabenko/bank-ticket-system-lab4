package com.example.fileservice.adapters.inbound.transaction;

import com.example.fileservice.application.service.ListFilesUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.ListFilesUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListFilesUseCaseTransactionalDecorator implements ListFilesUseCasePort {

    private final ListFilesUseCase delegate;

    public ListFilesUseCaseTransactionalDecorator(FileRepositoryPort fileRepositoryPort) {
        this.delegate = new ListFilesUseCase(fileRepositoryPort);
    }

    @Override
    public List<File> listAll(int page, int size) {
        return delegate.listAll(page, size);
    }
}
