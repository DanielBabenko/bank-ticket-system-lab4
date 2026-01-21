package com.example.productservice.domain.port.inbound;

import com.example.productservice.domain.model.entity.Product;

public interface CreateProductUseCasePort {
    Product create(String name, String description);
}
