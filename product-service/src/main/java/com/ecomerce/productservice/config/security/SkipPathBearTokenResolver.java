package com.ecomerce.productservice.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bearer Token Resolver cho Product Service
 * Skip JWT resolution cho các public endpoints (GET requests)
 */
@Component
public class SkipPathBearTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    /**
     * Các paths không cần JWT (public endpoints)
     * GET /api/products/** và GET /api/categories/** không cần JWT
     */
    private final List<String> skipPaths = List.of(
            "/api/products",
            "/api/categories",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health"
    );

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // GET requests đến /api/products/** hoặc /api/categories/** không cần JWT
        if ("GET".equals(method)) {
            if (path.startsWith("/api/products") || path.startsWith("/api/categories")) {
                return null;
            }
        }

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

