package com.ecomerce.authservice.service.impl;

import com.ecomerce.authservice.config.auth.AuthConfig;
import com.ecomerce.authservice.dto.request.auth.*;
import com.ecomerce.authservice.dto.response.auth.*;
import com.ecomerce.authservice.dto.response.user.UserProfileResponseDto;
import com.ecomerce.authservice.dto.response.user.UserSessionResponseDto;
import com.ecomerce.authservice.model.Role;
import com.ecomerce.authservice.model.User;
import com.ecomerce.authservice.repository.RoleRepository;
import com.ecomerce.authservice.repository.UserRepository;
import com.ecomerce.authservice.service.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final OtpRedisService  otpRedisService;
    private final EmailService emailService;


    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    @Override
    public UserSessionResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {
        if(userRepository.existsByEmail(userRegisterRequestDto.getEmail())) {
            throw new DataIntegrityViolationException("Email Already Exists");
        }

        User user = new User(
                userRegisterRequestDto.getName(),
                userRegisterRequestDto.getEmail(),
                passwordEncoder.encode(userRegisterRequestDto.getPassword())
        );
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chức vụ [USER] mặc định"));
        user.setRole(userRole);
        User savedUser = userRepository.saveAndFlush(user);
        return mapToUserInformation(savedUser);
    }




    @Override
    public AuthResult login(UserLoginRequestDto userLoginRequestDto) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userLoginRequestDto.getEmail(),
                userLoginRequestDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return buildAuthResult(email,userLoginRequestDto.getSessionMetaRequest());
    }

    @Override
    public ResponseCookie logout(String refreshToken) {

        if(refreshToken != null) {
            String email = jwtDecoder.decode(refreshToken).getSubject();
            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            refreshTokenRedisService.deleteRefreshToken(refreshToken, user.getId().toString());
        }
        return ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax") // Đổi từ Strict sang Lax để nhất quán
                .maxAge(0)
                .build();

    }

    @Override
    public AuthResult refresh(String refreshToken, SessionMetaRequest sessionMetaRequestDto) {
        String email = jwtDecoder.decode(refreshToken).getSubject();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));
        String userId = user.getId().toString();

        if (!refreshTokenRedisService.validateToken(refreshToken, userId))
            throw new BadJwtException(null);

        if (!user.getEmail().equalsIgnoreCase(email))
            throw new BadJwtException(null);

        refreshTokenRedisService.deleteRefreshToken(refreshToken, userId);

        return buildAuthResult(user, sessionMetaRequestDto);
    }

    @Override
    public List<SessionMetaResponseDto> getAllSelfSessionMeta(String refreshToken) {
        String email = jwtDecoder.decode(refreshToken).getSubject();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));
        String userId = user.getId().toString();

        return refreshTokenRedisService.getAllSessionMeta(userId, refreshToken);
    }

    @Override
    public UserProfileResponseDto getCurrentUserProfile() {
        String currentUserEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("user not found with"));

        return new UserProfileResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBirthDate(),
                user.getAddress(),
                user.getGender(),
                user.getLogoUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

    }

    @Override
    public UserSessionResponseDto getCurrentUserSession() {
        String currentUserEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return mapToUserInformation(currentUserEmail);
    }

    @Override
    public void removeSessionMeta(String sessionId) {
        String[] part = sessionId.split(":");
        String sessionUserId = part[3];

        String loginUserId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        User user = userRepository
                .findByEmail(loginUserId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        if (!user.getId().toString().equalsIgnoreCase(sessionUserId))
            throw new AccessDeniedException("Không có quyền truy cập");

        refreshTokenRedisService.deleteRefreshToken(sessionId);

    }

    @Override
    public OtpResponseDto sendOtpForPasswordReset(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        // Log để tracking - chỉ gửi email khi có HTTP request thực sự từ user
        log.info("🔐 [FORGOT PASSWORD] Request received for email: {}", forgotPasswordRequestDto.getEmail());
        
        User user = userRepository
                .findByEmail(forgotPasswordRequestDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy người dùng với email này"));

        // Kiểm tra nếu OTP còn hiệu lực
        if (otpRedisService.isOtpExist(forgotPasswordRequestDto.getEmail())) {
            log.warn("⚠️ [FORGOT PASSWORD] OTP still exists for email: {} - Rejecting request", forgotPasswordRequestDto.getEmail());
            throw new IllegalArgumentException(
                    "Mã OTP trước đó vẫn còn hiệu lực. Vui lòng kiểm tra email hoặc đợi 5 phút để gửi lại.");
        }

        // Kiểm tra rate limit
        if (!otpRedisService.canSendOtp(forgotPasswordRequestDto.getEmail())) {
            int attempts = otpRedisService.getSendAttempts(forgotPasswordRequestDto.getEmail());
            log.warn("⚠️ [FORGOT PASSWORD] Rate limit exceeded for email: {} - Attempts: {}", 
                    forgotPasswordRequestDto.getEmail(), attempts);
            throw new IllegalArgumentException(
                    "Bạn đã gửi OTP quá " + attempts + " lần. Vui lòng thử lại sau 15 phút.");
        }

        // Tạo mã OTP
        String otp = otpRedisService.generateOtp();
        log.info("✅ [FORGOT PASSWORD] Generated OTP for email: {}", forgotPasswordRequestDto.getEmail());

        // Lưu OTP vào Redis
        otpRedisService.saveOtp(forgotPasswordRequestDto.getEmail(), otp);

        // Tăng số lần gửi
        otpRedisService.incrementSendAttempt(forgotPasswordRequestDto.getEmail());

        // Gửi email - CHỈ được gọi từ HTTP request thực sự qua controller
        log.info("📧 [FORGOT PASSWORD] Sending OTP email to: {}", forgotPasswordRequestDto.getEmail());
        emailService.sendOtpEmail(forgotPasswordRequestDto.getEmail(), otp, user.getUsername());
        log.info("✅ [FORGOT PASSWORD] OTP email sent successfully to: {}", forgotPasswordRequestDto.getEmail());

        // Tính số lần gửi còn lại
        int currentAttempts = otpRedisService.getSendAttempts(forgotPasswordRequestDto.getEmail());
        int remainingAttempts = 3 - currentAttempts;

        return new OtpResponseDto(
                true,
                "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.",
                300L, // 5 phút = 300 giây
                remainingAttempts);
    }

    @Override
    public OtpResponseDto resendOtpForPasswordReset(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        User user = userRepository
                .findByEmail(forgotPasswordRequestDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy người dùng với email này"));

        // Kiểm tra rate limit
        if (!otpRedisService.canSendOtp(forgotPasswordRequestDto.getEmail())) {
            int attempts = otpRedisService.getSendAttempts(forgotPasswordRequestDto.getEmail());
            throw new IllegalArgumentException(
                    "Bạn đã gửi OTP quá " + attempts + " lần. Vui lòng thử lại sau 15 phút.");
        }

        // Xóa OTP cũ (nếu có)
        if (otpRedisService.isOtpExist(forgotPasswordRequestDto.getEmail())) {
            otpRedisService.deleteOtp(forgotPasswordRequestDto.getEmail());
        }

        // Tạo mã OTP mới
        String otp = otpRedisService.generateOtp();

        // Lưu OTP mới vào Redis
        otpRedisService.saveOtp(forgotPasswordRequestDto.getEmail(), otp);

        // Tăng số lần gửi
        otpRedisService.incrementSendAttempt(forgotPasswordRequestDto.getEmail());

        // Gửi email
        emailService.sendOtpEmail(forgotPasswordRequestDto.getEmail(), otp, user.getUsername());

        // Tính số lần gửi còn lại
        int currentAttempts = otpRedisService.getSendAttempts(forgotPasswordRequestDto.getEmail());
        int remainingAttempts = 3 - currentAttempts;

        return new OtpResponseDto(
                true,
                "Mã OTP mới đã được gửi đến email của bạn.",
                300L,
                remainingAttempts);
    }

    @Override
    public VerifyOtpResponseDto verifyOtpForPasswordReset(VerifyOtpRequestDto verifyOtpRequestDto) {
        userRepository
                .findByEmail(verifyOtpRequestDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        // Xác thực OTP
        boolean isValid = otpRedisService.verifyOtp(verifyOtpRequestDto.getEmail(), verifyOtpRequestDto.getOtp());

        String message = isValid
                ? "Mã OTP hợp lệ. Bạn có thể tiến hành đặt lại mật khẩu."
                : "Mã OTP không hợp lệ hoặc đã hết hạn.";

        return new VerifyOtpResponseDto(true, message, isValid);
    }

    @Override
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        User user = userRepository
                .findByEmail(resetPasswordRequestDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        // Xác thực OTP trước khi reset password
        if (!otpRedisService.verifyOtp(resetPasswordRequestDto.getEmail(), resetPasswordRequestDto.getOtp())) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getNewPassword()));
        userRepository.save(user);

        // Xóa OTP sau khi sử dụng
        otpRedisService.deleteOtp(resetPasswordRequestDto.getEmail());

        // Reset rate limit sau khi đổi mật khẩu thành công
        otpRedisService.resetRateLimit(resetPasswordRequestDto.getEmail());

        return new ResetPasswordResponseDto(
                true,
                "Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập với mật khẩu mới.");
    }

    private UserSessionResponseDto mapToUserInformation(User user) {
        if (user == null)
            throw new EntityNotFoundException("Không tìm thấy người dùng");

        Role role = user.getRole();
        List<String> permissions = null;
        if (user.getRole() != null && user.getRole().getPermissions() != null)
            permissions = role
                    .getPermissions()
                    .stream()
                    .map(x -> x.getMethod() + " " + x.getApiPath())
                    .toList();

        return new UserSessionResponseDto(
                user.getEmail(),
                user.getUsername(),
                user.getId(),
                role.getRoleName(),
                permissions,
                user.getLogoUrl(),
                user.getUpdatedAt().toString()
        );
    }
    private UserSessionResponseDto mapToUserInformation(String email) {
        if (email == null || email.isBlank())
            throw new EntityNotFoundException("Email không được để trống");

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        return mapToUserInformation(user);
    }

    private AuthResult buildAuthResult(String email, SessionMetaRequest sessionMetaRequest) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        return buildAuthResult(user, sessionMetaRequest);
    }

    private AuthResult buildAuthResult(User user, SessionMetaRequest sessionMetaRequest) {
        String refreshToken = buildJwt(refreshTokenExpiration, user);
        refreshTokenRedisService.saveRefreshToken(
                refreshToken,
                user.getId().toString(),
                sessionMetaRequest,
                Duration.ofSeconds(refreshTokenExpiration));

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax") // Đổi từ Strict sang Lax để cho phép cookie được gửi trong cross-site requests
                .maxAge(refreshTokenExpiration)
                .build();

        String accessToken = buildJwt(accessTokenExpiration, user);

        AuthTokenResponseDto authTokenResponseDto = new AuthTokenResponseDto(
                mapToUserInformation(user),
                accessToken);

        return new AuthResult(authTokenResponseDto, responseCookie);
    }

    private String buildJwt(Long expirationRate, User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(expirationRate, ChronoUnit.SECONDS);

        JwsHeader jwsHeader = JwsHeader.with(AuthConfig.MAC_ALGORITHM).build();

        Role role = user.getRole();
        List<String> permissions = role != null && role.getPermissions() != null
                ? role.getPermissions().stream().map(p -> p.getMethod() + " " + p.getApiPath()).toList()
                : List.of();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getEmail())
                .claim("user", mapToUserInformation(user))
                .claim("permissions", permissions)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
