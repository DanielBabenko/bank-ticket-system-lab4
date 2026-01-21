package com.example.productservice.adapters.outbound.persistence;

import com.example.productservice.adapters.outbound.persistence.entity.ProductJpaEntity;
import com.example.productservice.adapters.outbound.persistence.jpa.SpringProductJpaRepository;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringProductJpaRepository repository;

    public ProductRepositoryAdapter(SpringProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        repository.save(entity);
        return product;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return repository.findById(id)
                .map(e -> new Product(e.getId(), e.getName(), e.getDescription()));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public List<Product> findAll(int page, int size) {
        return repository.findAll()
                .stream()
                .map(e -> new Product(e.getId(), e.getName(), e.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Product product) {
        repository.deleteById(product.getId());
    }
}
