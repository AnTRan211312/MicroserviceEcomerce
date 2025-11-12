package com.ecomerce.paymentservice.config.feign;

import com.ecomerce.paymentservice.client.OrderServiceClient;
import com.ecomerce.paymentservice.client.dto.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderServiceFallbackFactory implements FallbackFactory<OrderServiceClient> {

    @Override
    public OrderServiceClient create(Throwable cause) {
        log.error("❌ OrderServiceClient fallback triggered: {}", cause.getMessage());
        
        return new OrderServiceClient() {
            @Override
            public OrderInfo getOrderById(Long orderId) {
                log.warn("⚠️ Order-service is unavailable. Using fallback for orderId: {}", orderId);
                throw new RuntimeException("Order-service is temporarily unavailable. Please try again later.");
            }
        };
    }
}

