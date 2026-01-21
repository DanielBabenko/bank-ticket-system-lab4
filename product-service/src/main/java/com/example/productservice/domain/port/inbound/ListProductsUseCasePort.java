package com.example.productservice.domain.port.inbound;

import com.example.productservice.domain.model.entity.Product;

import java.util.List;

public interface ListProductsUseCasePort {
    List<Product> list(int page, int size);
}
