package com.example.productservice.domain.port.inbound;

import com.example.productservice.domain.model.entity.Product;

import java.util.UUID;

public interface UpdateProductUseCasePort {
    Product update(UUID productId, String name, String description, UUID actorId, boolean isAdmin);
}
