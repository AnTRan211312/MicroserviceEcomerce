package com.ecomerce.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event nhận từ Kafka khi user thêm sản phẩm vào giỏ hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAddedEvent {
    private Long userId;
    private Long cartId;
    private Long cartItemId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private Instant timestamp;
}

