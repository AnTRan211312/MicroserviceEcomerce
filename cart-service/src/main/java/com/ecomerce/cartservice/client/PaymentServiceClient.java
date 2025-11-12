package com.ecomerce.cartservice.client;

import com.ecomerce.cartservice.client.dto.PaymentCreateResponse;
import com.ecomerce.cartservice.config.feign.FeignConfig;
import com.ecomerce.cartservice.config.feign.PaymentServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        configuration = FeignConfig.class,
        fallbackFactory = PaymentServiceFallbackFactory.class,
        path = "/api/payments"
)
public interface PaymentServiceClient {
    
    @PostMapping("/create")
    PaymentCreateResponse createPayment(@RequestBody PaymentCreateRequest request);
    
    // Inner class for request
    class PaymentCreateRequest {
        private Long orderId;
        private String paymentMethod;
        private String orderDescription;
        
        public PaymentCreateRequest() {}
        
        public PaymentCreateRequest(Long orderId, String paymentMethod, String orderDescription) {
            this.orderId = orderId;
            this.paymentMethod = paymentMethod;
            this.orderDescription = orderDescription;
        }
        
        public Long getOrderId() {
            return orderId;
        }
        
        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
        
        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
        
        public String getOrderDescription() {
            return orderDescription;
        }
        
        public void setOrderDescription(String orderDescription) {
            this.orderDescription = orderDescription;
        }
    }
}

