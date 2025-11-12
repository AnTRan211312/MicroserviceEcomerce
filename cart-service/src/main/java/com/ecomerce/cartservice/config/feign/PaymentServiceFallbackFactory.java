package com.ecomerce.cartservice.config.feign;

import com.ecomerce.cartservice.client.PaymentServiceClient;
import com.ecomerce.cartservice.client.dto.PaymentCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentServiceFallbackFactory implements FallbackFactory<PaymentServiceClient> {
    
    @Override
    public PaymentServiceClient create(Throwable cause) {
        return new PaymentServiceClient() {
            @Override
            public PaymentCreateResponse createPayment(PaymentServiceClient.PaymentCreateRequest request) {
                log.error("❌ Payment service unavailable. Cannot create payment for order: {}", 
                        request != null ? request.getOrderId() : "unknown", cause);
                throw new RuntimeException("Payment service không khả dụng. Vui lòng thử lại sau.");
            }
        };
    }
}

