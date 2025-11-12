package com.ecomerce.cartservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event được publish khi user thêm sản phẩm vào giỏ hàng
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

