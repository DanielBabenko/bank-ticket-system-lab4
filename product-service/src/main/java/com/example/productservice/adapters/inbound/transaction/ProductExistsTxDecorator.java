package com.example.productservice.adapters.inbound.transaction;

import com.example.productservice.application.usecase.ProductExistsUseCase;
import com.example.productservice.domain.port.inbound.ProductExistsUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductExistsTxDecorator implements ProductExistsUseCasePort {

    private final ProductExistsUseCase delegate;

    public ProductExistsTxDecorator(ProductRepositoryPort productRepositoryPort) {
        this.delegate = new ProductExistsUseCase(productRepositoryPort);
    }

    @Override
    public boolean existsById(UUID productId) {
        return delegate.existsById(productId);
    }
}
