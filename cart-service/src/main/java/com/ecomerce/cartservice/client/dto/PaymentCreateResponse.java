package com.ecomerce.cartservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResponse {
    private String paymentUrl;
    private Long paymentId;
    private String message;
    private String paymentMethod;
    private String paymentStatus;
}

