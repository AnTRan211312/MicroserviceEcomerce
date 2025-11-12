package com.ecomerce.paymentservice.client;

import com.ecomerce.paymentservice.client.dto.OrderInfo;
import com.ecomerce.paymentservice.config.feign.OrderServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "order-service",
        // Không set url để LoadBalancer tự động resolve từ Eureka
        path = "/api/orders",
        fallbackFactory = OrderServiceFallbackFactory.class
)
public interface OrderServiceClient {

    @GetMapping("/{id}")
    OrderInfo getOrderById(@PathVariable("id") Long orderId);
}

