package com.example.assignmentservice.adapters.infrastructure.client.adapter;

import com.example.assignmentservice.application.ports.ProductExistencePort;
import com.example.assignmentservice.domain.exception.ServiceUnavailableException;
import com.example.assignmentservice.adapters.infrastructure.client.feign.ProductServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductExistenceAdapter implements ProductExistencePort {
    private static final Logger log = LoggerFactory.getLogger(ProductExistenceAdapter.class);
    private final ProductServiceClient productServiceClient;

    public ProductExistenceAdapter(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    @Override
    public boolean existsById(UUID productId) {
        try {
            Boolean exists = productServiceClient.productExists(productId);
            if (exists == null) {
                log.warn("Product service returned null for product existence check: {}", productId);
                throw new ServiceUnavailableException("Product service unavailable");
            }
            return exists;
        } catch (Exception e) {
            log.error("Error checking product existence: {}", e.getMessage());
            throw new ServiceUnavailableException("Product service is unavailable");
        }
    }
}


