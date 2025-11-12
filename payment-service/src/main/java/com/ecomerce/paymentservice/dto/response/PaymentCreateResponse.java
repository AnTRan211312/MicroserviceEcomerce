package com.ecomerce.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response khi tạo payment
 * - VNPay: có paymentUrl để redirect user
 * - COD: paymentUrl = null, payment được auto-confirmed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResponse {
    private String paymentUrl; // null cho COD
    private Long paymentId;
    private String message;
    private String paymentMethod;
    private String paymentStatus;
}

