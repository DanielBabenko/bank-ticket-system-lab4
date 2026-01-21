package com.example.applicationservice.adapters.outbound.feign.client;

import com.example.applicationservice.application.exception.ServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public Boolean userExists(UUID id) {
                throw new ServiceUnavailableException("User service is unavailable now");
            }
        };
    }
}