package com.ecomerce.inventoryservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustRequest {

    @NotNull(message = "Số lượng điều chỉnh không được để trống")
    private Integer quantity;

    private String reason;
}

