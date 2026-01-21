package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.NotFoundException;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.GetProductUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;

import java.util.UUID;

public class GetProductUseCase implements GetProductUseCasePort {

    private final ProductRepositoryPort productRepository;

    public GetProductUseCase(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product getById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new NotFoundException("Product not found: " + productId)
                );
    }
}
