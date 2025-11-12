package com.ecomerce.cartservice.config.feign;

import com.ecomerce.cartservice.client.ProductServiceClient;
import com.ecomerce.cartservice.client.dto.ProductDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductServiceFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClientFallback(cause);
    }

    private static class ProductServiceClientFallback implements ProductServiceClient {
        private final Throwable cause;

        public ProductServiceClientFallback(Throwable cause) {
            this.cause = cause;
            log.warn("⚠️ ProductServiceClient fallback created due to: {}",
                    cause != null ? cause.getMessage() : "Unknown error");
        }

        @Override
        public ProductDetailResponse getProductById(Long id) {
            log.warn("⚠️ Circuit breaker opened or service unavailable. Fallback for getProductById({}). Cause: {}",
                    id, cause != null ? cause.getMessage() : "Unknown");
            return null;
        }
    }
}

