package com.ecomerce.inventoryservice.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkipPathBearTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    private final List<String> skipPaths = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health"
    );

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI();

        for (String skip : skipPaths) {
            if (path.equals(skip) || path.startsWith(skip + "/")) {
                return null;
            }
        }

        return delegate.resolve(request);
    }
}

