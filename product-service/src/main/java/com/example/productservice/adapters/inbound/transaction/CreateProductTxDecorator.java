package com.example.productservice.adapters.inbound.transaction;

import com.example.productservice.application.usecase.CreateProductUseCase;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.CreateProductUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateProductTxDecorator implements CreateProductUseCasePort {

    private final CreateProductUseCase delegate;

    public CreateProductTxDecorator(ProductRepositoryPort productRepositoryPort) {
        this.delegate = new CreateProductUseCase(productRepositoryPort);
    }

    @Override
    public Product create(String name, String description) {
        return delegate.create(name, description);
    }
}
