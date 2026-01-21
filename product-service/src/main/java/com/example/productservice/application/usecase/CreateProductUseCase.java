package com.example.productservice.application.usecase;

import com.example.productservice.application.dto.ProductRequest;
import com.example.productservice.application.exception.BadRequestException;
import com.example.productservice.application.exception.ConflictException;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.CreateProductUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;

import java.util.UUID;

public class CreateProductUseCase implements CreateProductUseCasePort {

    private final ProductRepositoryPort productRepository;

    public CreateProductUseCase(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Product name is required");
        }

        String trimmedName = name.trim();

        if (productRepository.existsByName(trimmedName)) {
            throw new ConflictException("Product name already in use");
        }

        Product product = Product.createNew(
                UUID.randomUUID(),
                trimmedName,
                description
        );

        return productRepository.save(product);
    }
}
