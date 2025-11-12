package com.ecomerce.inventoryservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để map từ ProductDetailResponse của product-service
 * Chỉ lấy các field cần thiết cho inventory-service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoResponse {
    private Long id;
    private String name;
    private Boolean active;
    // Có thể thêm các field khác nếu cần (slug, description, etc.)
}

