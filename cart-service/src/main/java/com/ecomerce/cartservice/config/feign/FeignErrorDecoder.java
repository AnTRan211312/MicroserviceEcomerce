package com.ecomerce.cartservice.config.feign;

import com.ecomerce.cartservice.advice.exeption.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Error Decoder để xử lý lỗi từ Feign Client
 * Chuyển đổi HTTP status codes thành các exception phù hợp
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());

        log.warn("Feign error for method: {}, status: {}, reason: {}",
                methodKey, status, response.reason());

        switch (status) {
            case NOT_FOUND:
                return new ResourceNotFoundException("Sản phẩm không tồn tại");
            case BAD_REQUEST:
                return new IllegalArgumentException("Request không hợp lệ từ product-service");
            case UNAUTHORIZED:
                return new SecurityException("Không có quyền truy cập product-service");
            case FORBIDDEN:
                return new SecurityException("Bị từ chối truy cập product-service");
            case SERVICE_UNAVAILABLE:
                return new RuntimeException("Product-service hiện không khả dụng");
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

