package com.ecomerce.authservice.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponseDto {
    private boolean success;
    private String message;
    private boolean isValid; // OTP có hợp lệ không
}
