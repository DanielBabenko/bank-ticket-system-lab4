package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.*;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.UpdateProductUseCasePort;
import com.example.productservice.domain.port.outbound.AssignmentServicePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;

import java.util.UUID;

public class UpdateProductUseCase implements UpdateProductUseCasePort {

    private final ProductRepositoryPort productRepository;
    private final AssignmentServicePort assignmentService;

    public UpdateProductUseCase(
            ProductRepositoryPort productRepository,
            AssignmentServicePort assignmentService
    ) {
        this.productRepository = productRepository;
        this.assignmentService = assignmentService;
    }

    @Override
    public Product update(UUID productId, String name, String description, UUID actorId, boolean isAdmin) {
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
                throw new ForbiddenException("Only ADMIN or PRODUCT_OWNER can update product");
            }
        }

        if (name != null && !name.trim().isEmpty()) {
            String trimmedName = name.trim();
            if (!trimmedName.equals(product.getName())
                    && productRepository.existsByName(trimmedName)) {
                throw new ConflictException("Product name already in use");
            }
            product.rename(trimmedName);
        }

        if (description != null) {
            product.changeDescription(description);
        }

        return productRepository.save(product);
    }
}
