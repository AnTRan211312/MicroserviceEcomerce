package com.ecomerce.inventoryservice.config.feign;

import com.ecomerce.inventoryservice.client.ProductServiceClient;
import com.ecomerce.inventoryservice.client.dto.ProductInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback Factory cho ProductServiceClient
 * Xử lý khi product-service down hoặc timeout
 */
@Slf4j
@Component
public class ProductServiceFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        log.error("❌ ProductServiceClient fallback triggered: {}", cause.getMessage());
        
        return new ProductServiceClient() {
            @Override
            public ProductServiceResponse<ProductInfoResponse> getProductById(Long productId) {
                log.warn("⚠️ Product-service is unavailable. Using fallback for productId: {}", productId);
                // Throw exception để service layer xử lý
                throw new RuntimeException("Product-service is temporarily unavailable. Please try again later.");
            }
        };
    }
}

