package com.ecomerce.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * ProductListResponse - Response tối giản cho list view
 * Chỉ chứa thông tin cần thiết để hiển thị danh sách
 */
@Data
@Builder
public class ProductListResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String thumbnail;
    private String categoryName; // Chỉ tên category, không phải object
    private boolean active;
    private Boolean featured;
}

