package com.ecomerce.cartservice.client;

import com.ecomerce.cartservice.client.dto.OrderCreateRequest;
import com.ecomerce.cartservice.client.dto.OrderResponse;
import com.ecomerce.cartservice.config.feign.OrderServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client để gọi order-service
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
        name = "order-service",
        // Không set url để LoadBalancer tự động resolve từ Eureka
        // url sẽ được resolve từ Eureka service registry
        path = "/api/orders",
        fallbackFactory = OrderServiceFallbackFactory.class
)
public interface OrderServiceClient {

    /**
     * Tạo đơn hàng mới
     * Gateway Secret và JWT token được tự động thêm bởi FeignConfig
     */
    @PostMapping
    OrderResponse createOrder(@RequestBody OrderCreateRequest request);
}

