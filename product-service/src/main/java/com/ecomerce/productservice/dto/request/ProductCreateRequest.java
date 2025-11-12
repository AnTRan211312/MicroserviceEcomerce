package com.ecomerce.productservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductCreateRequest - Tối giản hóa
 * Không nhận file, chỉ nhận URLs từ upload riêng
 */
@Data
public class ProductCreateRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không quá 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    private BigDecimal discountPrice;

    private LocalDateTime discountStartDate;

    private LocalDateTime discountEndDate;

    // URLs từ upload riêng - có thể null
    private String thumbnailUrl;

    // List URLs từ upload riêng - có thể null
    private List<String> imageUrls;

    @NotNull(message = "Category ID không được để trống")
    private Long categoryId;

    private boolean active = true;

    private Boolean featured = false;
}

