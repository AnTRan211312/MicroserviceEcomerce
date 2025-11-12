package com.ecomerce.inventoryservice.client;

import com.ecomerce.inventoryservice.client.dto.ProductInfoResponse;
import com.ecomerce.inventoryservice.config.feign.ProductServiceFallbackFactory;
import com.ecomerce.inventoryservice.config.feign.ProductServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client để gọi product-service
 * Sử dụng service name từ Eureka để discovery
 * 
 * Circuit Breaker và Retry được cấu hình qua:
 * - application.properties (resilience4j.*)
 * - FallbackFactory để xử lý khi service down
 * - Feign Client tự động tích hợp với Resilience4j khi có dependency
 * 
 * Lưu ý: Gateway Secret và JWT token được tự động thêm bởi FeignConfig.RequestInterceptor
 */
@FeignClient(
        name = "product-service",
        url = "${feign.client.product-service.url:}",
        path = "/api/products",
        fallbackFactory = ProductServiceFallbackFactory.class
)
public interface ProductServiceClient {

    /**
     * Lấy thông tin product theo ID (internal call)
     * Gateway Secret và JWT token được tự động thêm bởi FeignConfig
     * Có Circuit Breaker và Retry (cấu hình trong application.properties)
     * 
     * Note: Product-service trả về ApiResponse wrapper, nhưng Feign sẽ tự unwrap
     */
    @GetMapping("/{productId}")
    ProductServiceResponse<ProductInfoResponse> getProductById(@PathVariable("productId") Long productId);
}

