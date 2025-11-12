package com.ecomerce.cartservice.config.feign;

import com.ecomerce.cartservice.client.InventoryServiceClient;
import com.ecomerce.cartservice.client.dto.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryServiceFallbackFactory implements FallbackFactory<InventoryServiceClient> {

    @Override
    public InventoryServiceClient create(Throwable cause) {
        return new InventoryServiceClientFallback(cause);
    }

    private static class InventoryServiceClientFallback implements InventoryServiceClient {
        private final Throwable cause;

        public InventoryServiceClientFallback(Throwable cause) {
            this.cause = cause;
            log.warn("⚠️ InventoryServiceClient fallback created due to: {}",
                    cause != null ? cause.getMessage() : "Unknown error");
        }

        @Override
        public InventoryResponse getInventoryByProductId(Long productId) {
            log.warn("⚠️ Circuit breaker opened or service unavailable. Fallback for getInventoryByProductId({}). Cause: {}",
                    productId, cause != null ? cause.getMessage() : "Unknown");
            // Trả về null để caller có thể xử lý (có thể cho phép add to cart nếu inventory service down)
            return null;
        }
    }
}

