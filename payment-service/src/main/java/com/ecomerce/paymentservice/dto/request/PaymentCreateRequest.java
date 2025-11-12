package com.ecomerce.paymentservice.dto.request;

import com.ecomerce.paymentservice.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentCreateRequest {
    @NotNull(message = "Order ID không được để trống")
    private Long orderId;
    
    // Amount sẽ được lấy từ order, không cần client gửi
    // Nếu client gửi amount, sẽ validate với order.totalAmount
    
    private String orderDescription;
    
    /**
     * Payment method: VNPAY hoặc COD
     * Default: VNPAY (nếu không được specify)
     */
    @Pattern(regexp = "VNPAY|COD", message = "Phương thức thanh toán phải là VNPAY hoặc COD")
    private String paymentMethod;
    
    /**
     * Get payment method, default to VNPAY if not specified
     */
    public String getPaymentMethod() {
        return paymentMethod != null ? paymentMethod : PaymentMethod.getDefault();
    }
}

