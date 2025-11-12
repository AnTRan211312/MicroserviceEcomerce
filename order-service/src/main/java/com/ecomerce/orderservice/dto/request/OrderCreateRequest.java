package com.ecomerce.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateRequest {

    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String notes;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID không được để trống")
        private Long productId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer quantity;
        
        // Thông tin sản phẩm từ cart-service (đã được validate)
        private String productName;
        private String productImage;
        
        @NotNull(message = "Giá sản phẩm không được để trống")
        @DecimalMin(value = "0.01", message = "Giá sản phẩm phải lớn hơn 0")
        private BigDecimal price;
    }
}

