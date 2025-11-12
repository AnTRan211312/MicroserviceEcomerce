package com.ecomerce.orderservice.client;

import com.ecomerce.orderservice.client.dto.ProductDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        // Không set url để LoadBalancer tự động resolve từ Eureka
        path = "/api/products"
)
public interface ProductServiceClient {

    @GetMapping("/{id}")
    ProductDetailResponse getProductById(@PathVariable("id") Long id);
}

