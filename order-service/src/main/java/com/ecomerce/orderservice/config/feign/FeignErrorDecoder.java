package com.ecomerce.orderservice.config.feign;

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
        
        log.warn("⚠️ Feign error - Method: {}, Status: {}, Reason: {}", 
                methodKey, status.value(), status.getReasonPhrase());
        
        switch (status) {
            case NOT_FOUND:
                return new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Resource not found: " + methodKey);
            case BAD_REQUEST:
                return new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Bad request: " + methodKey);
            case UNAUTHORIZED:
                return new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "Unauthorized: " + methodKey);
            case FORBIDDEN:
                return new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Forbidden: " + methodKey);
            case INTERNAL_SERVER_ERROR:
                return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Internal server error: " + methodKey);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

