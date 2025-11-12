package com.ecomerce.productservice.advice.exeption;

public class InvalidImageDataException extends RuntimeException {
    public InvalidImageDataException(String message) {
        super(message);
    }
}
