package com.ecomerce.cartservice.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bearer Token Resolver cho Cart Service
 * Skip JWT resolution cho các public endpoints (nếu có)
 */
@Component
public class SkipPathBearTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    /**
     * Các paths không cần JWT (public endpoints)
     * Cart service thường không có public endpoints, nhưng giữ lại để consistent
     */
    private final List<String> skipPaths = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health"
    );

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip các monitoring/swagger endpoints
        for (String skip : skipPaths) {
            if (path.equals(skip) || path.startsWith(skip + "/")) {
                return null;
            }
        }

        // Các requests khác cần JWT
        return delegate.resolve(request);
    }
}

