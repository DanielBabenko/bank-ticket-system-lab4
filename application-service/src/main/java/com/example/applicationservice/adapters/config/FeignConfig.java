package com.example.applicationservice.adapters.config;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public feign.codec.Decoder feignDecoder() {
        return new JacksonDecoder();
    }

    @Bean
    public feign.codec.Encoder feignEncoder() {
        return new JacksonEncoder();
    }

    @Bean
    public feign.Contract feignContract() {
        return new SpringMvcContract();
    }

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .encoder(feignEncoder())
                .decoder(feignDecoder())
                .contract(feignContract());
    }
}