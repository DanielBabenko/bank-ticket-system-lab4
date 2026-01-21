package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.BadRequestException;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.ListProductsUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;

import java.util.List;

public class ListProductsUseCase implements ListProductsUseCasePort {

    private final ProductRepositoryPort productRepository;

    public ListProductsUseCase(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> list(int page, int size) {
        if (size <= 0) {
            throw new BadRequestException("Size must be positive");
        }
        return productRepository.findAll(page, size);
    }
}
