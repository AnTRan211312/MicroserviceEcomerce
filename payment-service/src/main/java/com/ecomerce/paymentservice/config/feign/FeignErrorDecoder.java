package com.ecomerce.paymentservice.config.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        String reason = response.reason();

        log.error("❌ Feign Client Error - Method: {}, Status: {}, Reason: {}", 
                methodKey, status, reason);

        switch (status) {
            case NOT_FOUND:
                return new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Order không tồn tại");
            case BAD_REQUEST:
                return new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Yêu cầu không hợp lệ từ order-service");
            case INTERNAL_SERVER_ERROR:
                return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "Lỗi từ order-service");
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

