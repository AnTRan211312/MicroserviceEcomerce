package com.ecomerce.orderservice.dto.request;

import com.ecomerce.orderservice.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderUpdateRequest {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private Order.OrderStatus status;

    private String notes;
}

