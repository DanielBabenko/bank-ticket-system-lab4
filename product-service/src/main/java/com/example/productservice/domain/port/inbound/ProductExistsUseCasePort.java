package com.example.productservice.domain.port.inbound;

import java.util.UUID;

public interface ProductExistsUseCasePort {
    boolean existsById(UUID productId);
}
