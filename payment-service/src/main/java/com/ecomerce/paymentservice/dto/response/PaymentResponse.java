package com.ecomerce.paymentservice.dto.response;

import com.ecomerce.paymentservice.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private String paymentMethod;
    private String vnpayTxnRef;
    private String vnpayTransactionNo;
    private String vnpayResponseCode;
    private String vnpayMessage;
    private Instant createdAt;
    private Instant updatedAt;
}

