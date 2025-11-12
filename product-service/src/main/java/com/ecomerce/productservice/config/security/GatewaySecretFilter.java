package com.ecomerce.productservice.config.security;

import com.ecomerce.productservice.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class GatewaySecretFilter implements Filter {

    @Value("${gateway.secret}")
    private String gatewaySecret;

    private final ObjectMapper objectMapper;

    private static final String GATEWAY_SECRET_HEADER = "X-Gateway-Secret";

    // Chỉ skip validation cho monitoring endpoints (health check, swagger, etc.)
    // TẤT CẢ các endpoints khác (bao gồm public endpoints) đều phải có Gateway Secret
    private static final List<String> SKIP_PATHS = List.of(
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // ? SKIP validation cho monitoring endpoints
        if (shouldSkipValidation(requestURI)) {
            log.debug("Skipping gateway secret validation for: {} {}", method, requestURI);
            chain.doFilter(request, response);
            return;
        }

        // ? VALIDATE Gateway Secret cho TẤT CẢ endpoints (bao gồm cả public endpoints)
        // Public endpoints vẫn cần Gateway Secret để đảm bảo request đi qua Gateway
        // (Nhưng không cần JWT - được xử lý ở SecurityConfig)
        String headerSecret = httpRequest.getHeader(GATEWAY_SECRET_HEADER);

        if (headerSecret == null) {
            log.warn("? Missing gateway secret - Direct access attempt from IP: {}, URI: {} {}",
                    httpRequest.getRemoteAddr(), method, requestURI);
            sendUnauthorizedResponse(httpResponse, "Truy cập trực tiếp không được phép. Vui lòng sử dụng API Gateway.");
            return;
        }

        if (!gatewaySecret.equals(headerSecret)) {
            log.warn("? Invalid gateway secret from IP: {}, URI: {} {}",
                    httpRequest.getRemoteAddr(), method, requestURI);
            sendUnauthorizedResponse(httpResponse, "Invalid gateway secret");
            return;
        }

        // ✅ Valid gateway secret
        log.debug("✅ Valid gateway secret for: {} {}", method, requestURI);
        chain.doFilter(request, response);
    }

    private boolean shouldSkipValidation(String uri) {
        return SKIP_PATHS.stream().anyMatch(uri::startsWith);
    }


    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = new ApiResponse<>(
                message,
                "UNAUTHORIZED"
        );

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("? GatewaySecretFilter initialized");
    }

    @Override
    public void destroy() {
        log.info("? GatewaySecretFilter destroyed");
    }
}