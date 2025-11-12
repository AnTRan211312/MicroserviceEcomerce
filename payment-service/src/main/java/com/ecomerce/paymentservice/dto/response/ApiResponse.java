package com.ecomerce.paymentservice.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ApiResponse<T> {
    private String message;
    private String error;
    private T data;

    public ApiResponse(String message, String error, T data) {
        this.message = message;
        this.error = error;
        this.data = data;
    }
}

