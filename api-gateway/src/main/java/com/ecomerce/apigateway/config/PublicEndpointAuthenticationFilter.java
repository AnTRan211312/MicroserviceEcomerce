package com.ecomerce.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter để remove Authorization header cho public endpoints
 * Giúp OAuth2 Resource Server không cố validate JWT cho public endpoints
 */
@Slf4j
@Component
public class PublicEndpointAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot",
            "/api/auth/verify-otp",
            "/api/auth/reset",
            "/api/auth/resend-otp",
            // Product service - Public endpoints (chỉ GET)
            "/api/products/**",
            "/api/categories/**"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        log.debug("🔍 PublicEndpointAuthenticationFilter - Path: {}, Method: {}, AuthHeader: {}", 
                path, method, authHeader != null ? "Present" : "Missing");
        
        // Nếu là public endpoint, remove Authorization header để OAuth2 Resource Server không validate JWT
        if (isPublicEndpoint(path)) {
            log.info("✅ Public endpoint detected: {} - Removing Authorization header", path);
            
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> headers.remove("Authorization"))
                    .build();
            
            return chain.filter(exchange.mutate().request(request).build());
        }
        
        log.debug("🔒 Protected endpoint: {} - Keeping Authorization header", path);
        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (pathMatches(path, publicEndpoint)) {
                return true;
            }
        }
        return false;
    }

    private boolean pathMatches(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    @Override
    public int getOrder() {
        // Chạy trước OAuth2 Resource Server filter (thường là -100)
        // Order càng nhỏ càng chạy trước
        return -200;
    }
}

