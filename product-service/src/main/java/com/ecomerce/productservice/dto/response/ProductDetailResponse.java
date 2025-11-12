package com.ecomerce.productservice.dto.response;

import com.ecomerce.productservice.dto.CategoryInfo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductDetailResponse - Cho trang chi tiết sản phẩm
 * Đầy đủ thông tin khi user click vào sản phẩm
 */
@Data
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private LocalDateTime discountStartDate;
    private LocalDateTime discountEndDate;
    private String thumbnail;
    private List<String> images;
    private CategoryInfo category;
    private boolean active;
    private Boolean featured;
}

