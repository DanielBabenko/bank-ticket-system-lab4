package com.example.productservice.adapters.inbound.transaction;

import com.example.productservice.application.usecase.ListProductsUseCase;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.inbound.ListProductsUseCasePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListProductsTxDecorator implements ListProductsUseCasePort {

    private final ListProductsUseCase delegate;

    public ListProductsTxDecorator(ProductRepositoryPort productRepositoryPort) {
        this.delegate = new ListProductsUseCase(productRepositoryPort);
    }

    @Override
    public List<Product> list(int page, int size) {
        return delegate.list(page, size);
    }
}
