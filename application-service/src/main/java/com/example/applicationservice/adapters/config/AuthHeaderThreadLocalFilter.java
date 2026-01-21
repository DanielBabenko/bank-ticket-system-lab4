package com.example.applicationservice.adapters.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderThreadLocalFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isBlank()) {
            AuthHeaderHolder.set(auth);
            return chain.filter(exchange)
                    .doFinally(sig -> AuthHeaderHolder.clear());
        } else {
            return chain.filter(exchange);
        }
    }
}

