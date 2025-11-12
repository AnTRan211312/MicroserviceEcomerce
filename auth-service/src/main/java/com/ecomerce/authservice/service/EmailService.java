package com.ecomerce.authservice.service;

public interface EmailService {
    void sendOtpEmail(String toEmail,String otp,String userName);
    String buildOtpEmailTemplate(String otp, String userName);
}
