package com.example.applicationservice.domain.port.inbound;

import java.util.UUID;

public interface DeleteApplicationsByUserIdUseCasePort {
    void deleteApplicationsByUserId(UUID userId);
}
