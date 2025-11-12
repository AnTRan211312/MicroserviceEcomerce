package com.ecomerce.orderservice.client;

import com.ecomerce.orderservice.client.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "inventory-service",
        // Không set url để LoadBalancer tự động resolve từ Eureka
        path = "/api/inventory"
)
public interface InventoryServiceClient {

    @GetMapping("/product/{productId}")
    InventoryResponse getInventoryByProductId(@PathVariable("productId") Long productId);
}

