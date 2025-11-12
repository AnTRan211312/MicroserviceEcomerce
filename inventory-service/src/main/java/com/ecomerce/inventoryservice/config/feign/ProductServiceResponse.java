package com.ecomerce.inventoryservice.config.feign;

import lombok.Data;

/**
 * Wrapper để map từ ApiResponse của product-service
 * Feign sẽ tự động deserialize từ ApiResponse<ProductDetailResponse> 
 * thành ProductServiceResponse<ProductInfoResponse>
 */
@Data
public class ProductServiceResponse<T> {
    private String message;
    private String code;
    private T data;
}

