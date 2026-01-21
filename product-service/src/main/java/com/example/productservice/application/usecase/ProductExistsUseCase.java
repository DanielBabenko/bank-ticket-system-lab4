package com.example.productservice.application.usecase;

import com.example.productservice.domain.port.inbound.ProductExistsUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;

import java.util.UUID;

public class ProductExistsUseCase implements ProductExistsUseCasePort {

    private final ProductRepositoryPort productRepository;

    public ProductExistsUseCase(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean existsById(UUID productId) {
        return productRepository.existsById(productId);
    }
}
