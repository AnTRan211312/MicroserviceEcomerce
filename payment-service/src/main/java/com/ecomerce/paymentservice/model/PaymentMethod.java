package com.ecomerce.paymentservice.model;

/**
 * Payment Method Constants
 * Enum-style class để define các phương thức thanh toán được hỗ trợ
 */
public class PaymentMethod {
    public static final String VNPAY = "VNPAY";
    public static final String COD = "COD"; // Cash on Delivery
    
    private PaymentMethod() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validate payment method
     * @param method payment method string
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String method) {
        return VNPAY.equals(method) || COD.equals(method);
    }
    
    /**
     * Get default payment method
     * @return default payment method (VNPAY)
     */
    public static String getDefault() {
        return VNPAY;
    }
}

