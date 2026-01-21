package com.example.applicationservice.adapters.outbound.feign;

import com.example.applicationservice.domain.port.outbound.ProductServicePort;
import com.example.applicationservice.adapters.outbound.feign.client.ProductServiceClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductServiceAdapter implements ProductServicePort {

    private final ProductServiceClient client;

    public ProductServiceAdapter(ProductServiceClient client) {
        this.client = client;
    }

    @Override
    public Boolean productExists(UUID productId) {
        return client.productExists(productId);
    }
}
