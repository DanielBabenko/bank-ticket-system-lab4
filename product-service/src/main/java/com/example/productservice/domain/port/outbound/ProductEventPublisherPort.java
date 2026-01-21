package com.example.productservice.domain.port.outbound;

import java.util.UUID;

public interface ProductEventPublisherPort {

    void publishProductDeleted(UUID productId);
}
