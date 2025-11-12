package com.ecomerce.authservice.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class SkipPathBearTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    private final List<String> skipPaths = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token", // Refresh token endpoint - không cần JWT, chỉ cần refresh_token cookie
            "/api/auth/forgot",
            "/api/auth/verify-otp",
            "/api/auth/reset",
            "/api/auth/resend-otp"
    );

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI();

        for (String skip : skipPaths) {
            // Match exact path hoặc path bắt đầu với skip path
            if (path.equals(skip) || path.startsWith(skip + "/")) {
                return null;
            }
        }

        return delegate.resolve(request);
    }
}
