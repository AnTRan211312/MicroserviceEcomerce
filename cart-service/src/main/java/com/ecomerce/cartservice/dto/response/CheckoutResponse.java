package com.ecomerce.cartservice.dto.response;

import com.ecomerce.cartservice.client.dto.OrderResponse;
import com.ecomerce.cartservice.client.dto.PaymentCreateResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private OrderResponse order;
    private List<Long> removedItemIds;
    private PaymentCreateResponse payment; // Payment info if paymentMethod was specified
}

