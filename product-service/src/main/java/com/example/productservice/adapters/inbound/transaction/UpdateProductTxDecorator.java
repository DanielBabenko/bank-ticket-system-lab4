package com.example.productservice.adapters.inbound.transaction;

import com.example.productservice.application.usecase.UpdateProductUseCase;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.UpdateProductUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import com.example.productservice.domain.port.outbound.AssignmentServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateProductTxDecorator implements UpdateProductUseCasePort {

    private final UpdateProductUseCase delegate;

    public UpdateProductTxDecorator(ProductRepositoryPort productRepositoryPort,
                                    AssignmentServicePort assignmentServicePort) {
        this.delegate = new UpdateProductUseCase(productRepositoryPort, assignmentServicePort);
    }

    @Override
    public Product update(UUID productId, String name, String description, UUID actorId, boolean isAdmin) {
        return delegate.update(productId, name, description, actorId, isAdmin);
    }
}
