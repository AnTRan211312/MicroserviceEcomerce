package com.ecomerce.paymentservice.service;

import com.ecomerce.paymentservice.dto.request.PaymentCreateRequest;
import com.ecomerce.paymentservice.dto.response.PaymentCreateResponse;
import com.ecomerce.paymentservice.dto.response.PaymentResponse;
import com.ecomerce.paymentservice.dto.response.PageResponseDto;
import com.ecomerce.paymentservice.dto.response.VnpayPaymentUrlResponse;
import com.ecomerce.paymentservice.model.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PaymentService {
    PaymentCreateResponse createPayment(Long userId, PaymentCreateRequest request, HttpServletRequest httpRequest);
    
    /**
     * Create payment without HttpServletRequest (for Feign clients)
     * For COD: HttpServletRequest is not needed
     * For VNPay: IP address will be extracted from request headers if available
     */
    PaymentCreateResponse createPaymentWithoutRequest(Long userId, PaymentCreateRequest request);
    
    // Backward compatibility - deprecated, use createPayment instead
    @Deprecated
    default VnpayPaymentUrlResponse createPaymentLegacy(Long userId, PaymentCreateRequest request, HttpServletRequest httpRequest) {
        PaymentCreateResponse response = createPayment(userId, request, httpRequest);
        return new VnpayPaymentUrlResponse(response.getPaymentUrl(), response.getPaymentId(), response.getMessage());
    }
    
    PaymentResponse handleVnpayCallback(HttpServletRequest request);
    PaymentResponse getPaymentById(Long paymentId, Long userId);
    PaymentResponse getPaymentByOrderId(Long orderId, Long userId);
    List<PaymentResponse> getUserPayments(Long userId);
    PageResponseDto<PaymentResponse> getAllPayments(Specification<Payment> spec, Pageable pageable);
    
    // COD Payment methods
    PaymentResponse completeCodPayment(Long orderId, Long userId);
}

