package com.example.productservice.domain.port.outbound;

import com.example.productservice.domain.model.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    boolean existsById(UUID id);

    boolean existsByName(String name);

    List<Product> findAll(int page, int size);

    void delete(Product product);
}
