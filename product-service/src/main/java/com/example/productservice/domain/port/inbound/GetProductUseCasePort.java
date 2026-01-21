package com.example.productservice.domain.port.inbound;

import com.example.productservice.domain.model.entity.Product;

import java.util.UUID;

public interface GetProductUseCasePort {
    Product getById(UUID productId);
}
