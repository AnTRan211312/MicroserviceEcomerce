package com.ecomerce.authservice.controller;

import com.ecomerce.authservice.annontaton.ApiMessage;
import com.ecomerce.authservice.dto.request.auth.*;
import com.ecomerce.authservice.dto.response.ApiResponse;
import com.ecomerce.authservice.dto.response.auth.*;
import com.ecomerce.authservice.dto.response.user.UserProfileResponseDto;
import com.ecomerce.authservice.dto.response.user.UserSessionResponseDto;
import com.ecomerce.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ApiMessage(value = "Đăng ký thành công")
    @Operation(summary = "Người dùng đăng ký")
    @SecurityRequirements()
    public ResponseEntity<UserSessionResponseDto> register(
            @Valid
            @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        return ResponseEntity.ok(authService.register(userRegisterRequestDto));
    }

    @PostMapping("/login")
    @ApiMessage(value = "Người dùng đăng nhập thành công")
    @Operation(summary = "Người dùng đăng nhập")
    @SecurityRequirements()
    public ResponseEntity<AuthTokenResponseDto> login(
            @Valid @RequestBody UserLoginRequestDto userLoginRequestDto
    ){
        AuthResult authResult = authService.login(userLoginRequestDto);
        AuthTokenResponseDto authTokenResponseDto = authResult.getAuthTokenResponseDto();
        ResponseCookie responseCookie =authResult.getResponseCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(authTokenResponseDto);
    }

    @PostMapping("/logout")
    @ApiMessage(value = "Người dùng đăng xuất thành công")
    @Operation(summary = "Người dùng đăng xuất")
    @SecurityRequirements()
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refresh_token",required = false) String refreshToken
    ){
        ResponseCookie responseCookie = authService.logout(refreshToken);
        return ResponseEntity.ok().
                header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .build();
    }
    @GetMapping("/me")
    @ApiMessage(value = "Trả về thông tin phiên đăng nhập của người dùng hiện tại")
    @Operation(summary = "Lấy thông tin phiên đăng nhập của người dùng hiện tại")
    public ResponseEntity<UserSessionResponseDto> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserSession());
    }

    @GetMapping("/me/details")
    @ApiMessage(value = "Trả về thông tin chi tiết của người dùng hiện tại")
    @Operation(summary = "Lấy thông tin chi tiết của người dùng hiện tại")
    public ResponseEntity<UserProfileResponseDto> getCurrentUserDetails() {
        return ResponseEntity.ok(authService.getCurrentUserProfile());
    }

    @PostMapping("/refresh-token")
    @ApiMessage(value = "Lấy refresh token")
    @Operation(summary = "Cấp lại access token và refresh token mới")
    @SecurityRequirements() // Không yêu cầu authentication
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            @RequestBody SessionMetaRequest sessionMetaRequest
    ) {
        // Kiểm tra nếu không có refresh token
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                            "Refresh token không tồn tại hoặc đã hết hạn. Vui lòng đăng nhập lại.",
                            "REFRESH_TOKEN_NOT_FOUND"
                    ));
        }
        
        try {
            AuthResult authResult = authService.refresh(refreshToken, sessionMetaRequest);

            AuthTokenResponseDto authTokenResponseDto = authResult.getAuthTokenResponseDto();
            ResponseCookie responseCookie = authResult.getResponseCookie();

            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(authTokenResponseDto);
        } catch (Exception e) {
            // Xử lý các lỗi khác (ví dụ: token không hợp lệ, token đã hết hạn, etc.)
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                            "Refresh token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.",
                            "REFRESH_TOKEN_INVALID"
                    ));
        }
    }
    @GetMapping("/sessions")
    @ApiMessage(value = "Lấy session")
    @Operation(summary = "Lấy tất cả phiên đăng nhập của người dùng hiện tại")
    public ResponseEntity<List<SessionMetaResponseDto>> getAllSelfSessionMetas(@CookieValue(value = "refresh_token") String refreshToken) {
        return ResponseEntity.ok(authService.getAllSelfSessionMeta(refreshToken));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ApiMessage(value = "Xóa session")
    @Operation(summary = "Xóa phiên đăng nhập của người dùng theo id phiên")
    public ResponseEntity<Void> removeSelfSession(@PathVariable String sessionId) {
        authService.removeSessionMeta(sessionId);

        return ResponseEntity.ok().build();
    }

    /**
     * Bước 1: Gửi mã OTP đến email
     * POST /auth/password/forgot
     */
    @PostMapping("/forgot")
    @ApiMessage(value = "Mã OTP đã được gửi đến email")
    @Operation(summary = "Gửi mã OTP để đặt lại mật khẩu")
    @SecurityRequirements()
    public ResponseEntity<OtpResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request
    ) {
        log.info("📥 [CONTROLLER] /api/auth/forgot endpoint called for email: {}", request.getEmail());
        OtpResponseDto response = authService.sendOtpForPasswordReset(request);
        log.info("✅ [CONTROLLER] /api/auth/forgot completed successfully for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Bước 1.5: Gửi lại mã OTP (Resend)
     * POST /auth/password/resend-otp
     */
    @PostMapping("/resend-otp")
    @ApiMessage(value = "Đã gửi lại mã OTP")
    @Operation(summary = "Gửi lại mã OTP mới")
    @SecurityRequirements()
    public ResponseEntity<OtpResponseDto> resendOtp(
            @Valid @RequestBody ForgotPasswordRequestDto request
    ) {
        OtpResponseDto response = authService.resendOtpForPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Bước 2: Xác thực mã OTP (Optional)
     * POST /auth/password/verify-otp
     */
    @PostMapping("/verify-otp")
    @ApiMessage(value = "Xác thực mã OTP")
    @Operation(summary = "Xác thực mã OTP (tùy chọn)")
    @SecurityRequirements()
    public ResponseEntity<VerifyOtpResponseDto> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto request
    ) {
        VerifyOtpResponseDto response = authService.verifyOtpForPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Bước 3: Reset mật khẩu với OTP đã xác thực
     * POST /auth/password/reset
     */
    @PostMapping("/reset")
    @ApiMessage(value = "Đặt lại mật khẩu thành công")
    @Operation(summary = "Đặt lại mật khẩu với mã OTP")
    @SecurityRequirements()
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request
    ) {
        ResetPasswordResponseDto response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}