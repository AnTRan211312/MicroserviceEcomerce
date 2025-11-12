package com.ecomerce.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * CategoryAdminResponse - Cho giao diện admin
 * Đầy đủ thông tin + ngày giờ tạo/cập nhật
 */
@Data
@Builder
public class CategoryAdminResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    
    // Thông tin audit cho admin
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;
}

