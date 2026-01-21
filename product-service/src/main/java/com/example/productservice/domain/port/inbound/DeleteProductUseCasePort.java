package com.example.productservice.domain.port.inbound;

import java.util.UUID;

public interface DeleteProductUseCasePort {
    void delete(UUID productId, UUID actorId, boolean isAdmin);
}
