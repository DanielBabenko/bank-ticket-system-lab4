package com.example.applicationservice.domain.port.inbound;

import java.util.UUID;

public interface DeleteApplicationsByProductIdUseCasePort {
    void deleteApplicationsByProductId(UUID productId);
}
