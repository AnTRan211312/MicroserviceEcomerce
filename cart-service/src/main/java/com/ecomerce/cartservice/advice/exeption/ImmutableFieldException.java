package com.ecomerce.cartservice.advice.exeption;

public class ImmutableFieldException extends RuntimeException {
    public ImmutableFieldException(String message) {
        super(message);
    }
}

