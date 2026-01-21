package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.*;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.DeleteProductUseCasePort;
import com.example.productservice.domain.port.outbound.AssignmentServicePort;
import com.example.productservice.domain.port.outbound.ProductEventPublisherPort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;

import java.util.UUID;

public class DeleteProductUseCase implements DeleteProductUseCasePort {

    private final ProductRepositoryPort productRepository;
    private final AssignmentServicePort assignmentService;
    private final ProductEventPublisherPort eventPublisher;

    public DeleteProductUseCase(
            ProductRepositoryPort productRepository,
            AssignmentServicePort assignmentService,
            ProductEventPublisherPort eventPublisher
    ) {
        this.productRepository = productRepository;
        this.assignmentService = assignmentService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void delete(UUID productId, UUID actorId, boolean isAdmin) {
        if (actorId == null) {
            throw new UnauthorizedException("Authentication required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new NotFoundException("Product not found: " + productId)
                );

        if (!isAdmin) {
            Boolean isOwner = assignmentService.hasRole(
                    actorId,
                    productId,
                    "PRODUCT_OWNER"
            );

            if (isOwner == null) {
                throw new ServiceUnavailableException("Assignment service unavailable");
            }
            if (!isOwner) {
                throw new ForbiddenException("Only ADMIN or PRODUCT_OWNER can delete product");
            }
        }

        productRepository.delete(product);
        eventPublisher.publishProductDeleted(productId);
    }
}
