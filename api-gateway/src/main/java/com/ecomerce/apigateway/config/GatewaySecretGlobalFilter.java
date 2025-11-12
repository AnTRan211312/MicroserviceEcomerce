package com.ecomerce.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global Filter để validate GATEWAY_SECRET từ incoming requests
 * Đảm bảo các request đến Gateway có GATEWAY_SECRET hợp lệ (nếu cần)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewaySecretGlobalFilter implements GlobalFilter, Ordered {

    @Value("${gateway.secret}")
    private String gatewaySecret;

    private final ObjectMapper objectMapper;

    private static final String GATEWAY_SECRET_HEADER = "X-Gateway-Secret";

    /**
     * Các paths không cần validate GATEWAY_SECRET
     */
    private static final List<String> SKIP_PATHS = List.of(
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip validation cho monitoring endpoints
        if (shouldSkipValidation(path)) {
            return chain.filter(exchange);
        }

        // Validate GATEWAY_SECRET từ incoming request (nếu có)
        // Note: Gateway thường không cần validate GATEWAY_SECRET từ client
        // vì GATEWAY_SECRET chỉ dùng để Gateway forward đến services
        // Nhưng nếu muốn validate, có thể uncomment phần dưới

        String headerSecret = request.getHeaders().getFirst(GATEWAY_SECRET_HEADER);
        
        // Nếu có header GATEWAY_SECRET, validate nó
        if (headerSecret != null && !gatewaySecret.equals(headerSecret)) {
            log.warn("Invalid gateway secret from IP: {}, Path: {}",
                    request.getRemoteAddress(), path);
            return handleError(exchange, "Invalid gateway secret", HttpStatus.UNAUTHORIZED);
        }

        // Tiếp tục với request
        return chain.filter(exchange);
    }

    private boolean shouldSkipValidation(String path) {
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> handleError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        errorResponse.put("status", status.name());

        try {
            String json = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing error response", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // Chạy trước các filter khác, nhưng sau CORS
        return -100;
    }
}

