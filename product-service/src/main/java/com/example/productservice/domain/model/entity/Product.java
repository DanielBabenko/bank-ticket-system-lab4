package com.example.productservice.domain.model.entity;

import java.util.UUID;

public class Product {

    private final UUID id;
    private String name;
    private String description;

    public Product(UUID id, String name, String description) {
        if (id == null) throw new IllegalArgumentException("Product id is required");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        this.id = id;
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
    }

    public static Product createNew(UUID id, String name, String description) {
        return new Product(id, name, description);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        this.name = newName.trim();
    }

    public String getDescription() {
        return description;
    }

    public void changeDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }
}
