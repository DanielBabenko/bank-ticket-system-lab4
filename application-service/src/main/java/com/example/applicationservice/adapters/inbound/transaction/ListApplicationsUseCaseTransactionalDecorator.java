package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.ListApplicationsUseCase;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.ListApplicationsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.FileServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListApplicationsUseCaseTransactionalDecorator implements ListApplicationsUseCasePort {

    private final ListApplicationsUseCase delegate;

    public ListApplicationsUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                         FileServicePort fileServicePort) {
        this.delegate = new ListApplicationsUseCase(applicationRepositoryPort, fileServicePort);
    }

    @Override
    public List<Application> listApplications(int page, int size) {
        return delegate.listApplications(page, size);
    }
}
