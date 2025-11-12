package com.ecomerce.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InventoryUpdateRequest {

    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    @Min(value = 0, message = "Ngưỡng cảnh báo tồn kho thấp phải lớn hơn hoặc bằng 0")
    private Integer lowStockThreshold;

    private Boolean isActive;
}

