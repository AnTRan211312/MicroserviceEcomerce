package com.ecomerce.orderservice.advice.exeption;

public class ImmutableFieldException extends RuntimeException {
    public ImmutableFieldException(String message) {
        super(message);
    }
}

