package com.ecomerce.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VnpayPaymentUrlResponse {
    private String paymentUrl;
    private Long paymentId;
    private String message;
}

