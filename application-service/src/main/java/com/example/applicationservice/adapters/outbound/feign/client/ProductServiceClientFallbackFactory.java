package com.example.applicationservice.adapters.outbound.feign.client;

import com.example.applicationservice.application.exception.ServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {
    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {
            @Override
            public Boolean productExists(UUID id) {
                throw new ServiceUnavailableException("Product service is unavailable now");
            }
        };
    }
}
