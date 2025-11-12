package com.ecomerce.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * ProductSummaryResponse - Cho trang web (danh sách sản phẩm)
 * Chỉ hiển thị: ảnh, tên, giá
 * Tối ưu cho performance - minimal data
 */
@Data
@Builder
public class ProductSummaryResponse {
    private Long id;
    private String name;
    private String thumbnail;
    private BigDecimal price;
    private BigDecimal discountPrice;
}