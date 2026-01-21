package com.example.applicationservice.domain.port.outbound;

import java.util.UUID;

public interface ProductServicePort {
    Boolean productExists(UUID productId);
}
