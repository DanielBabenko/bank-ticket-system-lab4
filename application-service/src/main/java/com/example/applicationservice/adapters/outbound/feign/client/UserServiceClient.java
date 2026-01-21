package com.example.applicationservice.adapters.outbound.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{id}/exists")
    Boolean userExists(@PathVariable("id") UUID id);
}