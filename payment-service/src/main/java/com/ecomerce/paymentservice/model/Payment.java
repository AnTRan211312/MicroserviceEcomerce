package com.ecomerce.paymentservice.model;

import com.ecomerce.paymentservice.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_vnpay_txn_ref", columnList = "vnpay_txn_ref")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class Payment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;
    
    @Column(name = "vnpay_txn_ref", length = 255)
    private String vnpayTxnRef;
    
    @Column(name = "vnpay_transaction_no", length = 255)
    private String vnpayTransactionNo;
    
    @Column(name = "vnpay_response_code", columnDefinition = "TEXT")
    private String vnpayResponseCode;
    
    @Column(name = "vnpay_message", columnDefinition = "TEXT")
    private String vnpayMessage;
    
    @Column(name = "callback_data", columnDefinition = "TEXT")
    private String callbackData;
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        CANCELLED,
        REFUNDED
    }
}

