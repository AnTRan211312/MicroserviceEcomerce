package com.ecomerce.productservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductUpdateRequest - Tối giản hóa, tất cả fields optional
 * Không nhận file, chỉ nhận URLs từ upload riêng
 * Dùng cho PUT - full update (tất cả fields optional)
 */
@Data
public class ProductUpdateRequest {

    @Size(max = 255, message = "Tên sản phẩm không quá 255 ký tự")
    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private LocalDateTime discountStartDate;

    private LocalDateTime discountEndDate;

    // URLs từ upload riêng - có thể null (nếu không thay đổi)
    private String thumbnailUrl;

    // List URLs từ upload riêng - có thể null (nếu không thay đổi)
    private List<String> imageUrls;

    private Long categoryId;

    private Boolean active;

    private Boolean featured;
}

