package com.ecomerce.authservice.service;

import com.ecomerce.authservice.dto.request.auth.*;
import com.ecomerce.authservice.dto.response.auth.*;
import com.ecomerce.authservice.dto.response.user.UserProfileResponseDto;
import com.ecomerce.authservice.dto.response.user.UserSessionResponseDto;
import org.springframework.http.ResponseCookie;

import java.util.List;

public interface AuthService {
    UserSessionResponseDto register(UserRegisterRequestDto userRegisterRequestDto);

    AuthResult login(UserLoginRequestDto userLoginRequestDto);

    ResponseCookie logout(String refreshToken);

    AuthResult refresh(String refreshToken, SessionMetaRequest sessionMetaRequestDto);

    List<SessionMetaResponseDto> getAllSelfSessionMeta(String refreshToken);

    UserProfileResponseDto getCurrentUserProfile();

    UserSessionResponseDto getCurrentUserSession();

    void removeSessionMeta(String sessionId);

    OtpResponseDto sendOtpForPasswordReset(ForgotPasswordRequestDto forgotPasswordRequestDto);

    OtpResponseDto resendOtpForPasswordReset(ForgotPasswordRequestDto forgotPasswordRequestDto);

    VerifyOtpResponseDto verifyOtpForPasswordReset(VerifyOtpRequestDto verifyOtpRequestDto);

    ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto resetPasswordRequestDto);
}
