package com.ecomerce.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryCreateRequest {

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    @Min(value = 0, message = "Ngưỡng cảnh báo tồn kho thấp phải lớn hơn hoặc bằng 0")
    private Integer lowStockThreshold;
}

