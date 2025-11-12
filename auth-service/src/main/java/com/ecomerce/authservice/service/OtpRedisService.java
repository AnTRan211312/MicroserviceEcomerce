package com.ecomerce.authservice.service;

public interface OtpRedisService {

    String generateOtp();

    void saveOtp(String email, String otp);


    boolean verifyOtp(String email, String otp);


    void deleteOtp(String email);


    boolean isOtpExist(String email);


    boolean canSendOtp(String email);


    void incrementSendAttempt(String email);


    int getSendAttempts(String email);


    void resetRateLimit(String email);
}
