package com.example.productservice.adapters.inbound.transaction;

import com.example.productservice.application.usecase.DeleteProductUseCase;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.DeleteProductUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import com.example.productservice.domain.port.outbound.AssignmentServicePort;
import com.example.productservice.domain.port.outbound.ProductEventPublisherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteProductTxDecorator implements DeleteProductUseCasePort {

    private final DeleteProductUseCase delegate;

    public DeleteProductTxDecorator(ProductRepositoryPort productRepositoryPort,
                                    AssignmentServicePort assignmentServicePort,
                                    ProductEventPublisherPort eventPublisher) {
        this.delegate = new DeleteProductUseCase(productRepositoryPort, assignmentServicePort, eventPublisher);
    }

    @Override
    public void delete(UUID productId, UUID actorId, boolean isAdmin) {
        delegate.delete(productId, actorId, isAdmin);
    }
}
