package com.example.productservice.adapters.inbound.transaction;

import com.example.productservice.application.usecase.GetProductUseCase;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.GetProductUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetProductTxDecorator implements GetProductUseCasePort {

    private final GetProductUseCase delegate;

    public GetProductTxDecorator(ProductRepositoryPort productRepositoryPort) {
        this.delegate = new GetProductUseCase(productRepositoryPort);
    }

    @Override
    public Product getById(UUID productId) {
        return delegate.getById(productId);
    }
}
