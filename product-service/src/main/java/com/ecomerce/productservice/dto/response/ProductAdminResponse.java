package com.ecomerce.productservice.dto.response;

import com.ecomerce.productservice.dto.CategoryInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductAdminResponse - Cho giao diện admin
 * Đầy đủ thông tin + ngày giờ tạo/cập nhật
 */
@Data
@Builder
public class ProductAdminResponse {
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
    
    // Thông tin audit cho admin
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;
}

