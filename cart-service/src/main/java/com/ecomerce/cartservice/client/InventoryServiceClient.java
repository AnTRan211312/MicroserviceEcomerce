package com.ecomerce.cartservice.client;

import com.ecomerce.cartservice.client.dto.InventoryResponse;
import com.ecomerce.cartservice.config.feign.InventoryServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client để gọi inventory-service
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
        name = "inventory-service",
        // Không set url để LoadBalancer tự động resolve từ Eureka
        path = "/api/inventory",
        fallbackFactory = InventoryServiceFallbackFactory.class
)
public interface InventoryServiceClient {

    /**
     * Lấy thông tin kho hàng theo Product ID (public endpoint)
     * Gateway Secret và JWT token được tự động thêm bởi FeignConfig
     */
    @GetMapping("/product/{productId}")
    InventoryResponse getInventoryByProductId(@PathVariable("productId") Long productId);
}

