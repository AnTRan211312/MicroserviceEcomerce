package com.ecomerce.authservice.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResult {

    private AuthTokenResponseDto authTokenResponseDto;
    private ResponseCookie responseCookie;
}
