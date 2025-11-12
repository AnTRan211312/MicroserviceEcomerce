package com.ecomerce.cartservice.config.feign;

import com.ecomerce.cartservice.client.OrderServiceClient;
import com.ecomerce.cartservice.client.dto.OrderCreateRequest;
import com.ecomerce.cartservice.client.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderServiceFallbackFactory implements FallbackFactory<OrderServiceClient> {

    @Override
    public OrderServiceClient create(Throwable cause) {
        return new OrderServiceClientFallback(cause);
    }

    private static class OrderServiceClientFallback implements OrderServiceClient {
        private final Throwable cause;

        public OrderServiceClientFallback(Throwable cause) {
            this.cause = cause;
            log.warn("⚠️ OrderServiceClient fallback created due to: {}",
                    cause != null ? cause.getMessage() : "Unknown error");
        }

        @Override
        public OrderResponse createOrder(OrderCreateRequest request) {
            log.warn("⚠️ Circuit breaker opened or service unavailable. Fallback for createOrder(). Cause: {}",
                    cause != null ? cause.getMessage() : "Unknown");
            throw new RuntimeException("Order-service hiện không khả dụng. Vui lòng thử lại sau.");
        }
    }
}

