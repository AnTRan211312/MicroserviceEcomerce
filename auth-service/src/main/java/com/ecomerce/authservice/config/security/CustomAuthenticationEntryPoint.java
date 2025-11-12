package com.ecomerce.authservice.config.security;


import com.ecomerce.authservice.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper objectMapper;

    // Public endpoints không cần JWT - nếu lỗi authentication ở đây, bỏ qua (không trả về 401)
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token", // Refresh token endpoint - không cần JWT, chỉ cần refresh_token cookie
            "/api/auth/forgot",
            "/api/auth/verify-otp",
            "/api/auth/reset",
            "/api/auth/resend-otp"
    );

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        String path = request.getRequestURI();
        
        // Nếu là public endpoint, KHÔNG trả về lỗi 401
        // Để request tiếp tục đến authorization filter (permitAll sẽ cho phép)
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (path.equals(publicEndpoint) || path.startsWith(publicEndpoint + "/")) {
                // Không làm gì cả - để authorization filter xử lý với permitAll()
                // KHÔNG gọi delegate.commence() để tránh set 401 status
                return;
            }
        }
        
        // Protected endpoints - trả về 401
        delegate.commence(request, response, authenticationException);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = new ApiResponse<>(
                "Token không hợp lệ (không đúng định dạng, hết hạn)",
                "UNAUTHORIZED"
        );

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

}
