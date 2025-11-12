package com.ecomerce.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Global Filter to ensure UTF-8 encoding for all responses
 * Đảm bảo tất cả responses đều có charset=UTF-8 để hiển thị tiếng Việt đúng
 */
@Slf4j
@Component
public class EncodingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();

        // Set UTF-8 charset cho Content-Type header
        HttpHeaders headers = response.getHeaders();
        
        // Nếu response có Content-Type nhưng chưa có charset, thêm charset=UTF-8
        MediaType contentType = headers.getContentType();
        if (contentType != null && contentType.getCharset() == null) {
            MediaType newContentType = new MediaType(
                contentType.getType(),
                contentType.getSubtype(),
                StandardCharsets.UTF_8
            );
            headers.setContentType(newContentType);
        }

        // Đặc biệt xử lý cho JSON responses (Swagger API docs)
        String requestPath = exchange.getRequest().getURI().getPath();
        if (requestPath != null && (requestPath.contains("/v3/api-docs") || 
                                    requestPath.contains("/swagger-ui") ||
                                    requestPath.contains("/api/"))) {
            if (headers.getContentType() == null || 
                headers.getContentType().equals(MediaType.APPLICATION_JSON)) {
                headers.setContentType(new MediaType(
                    MediaType.APPLICATION_JSON,
                    StandardCharsets.UTF_8
                ));
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Chạy sau GatewaySecretGlobalFilter nhưng trước response
        return 1;
    }
}

