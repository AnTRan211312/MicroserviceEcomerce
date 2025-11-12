package com.ecomerce.productservice.advice.exeption;

public class S3UploadException extends RuntimeException {
    public S3UploadException(String message) {
        super(message);
    }
}