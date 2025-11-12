package com.ecomerce.paymentservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

public class JwtUtil {

    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                Map<String, Object> userClaim = jwt.getClaim("user");
                if (userClaim != null && userClaim.containsKey("id")) {
                    Object id = userClaim.get("id");
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                    if (id instanceof String) {
                        return Long.parseLong((String) id);
                    }
                }
            }
        } catch (Exception e) {
            // Log error nếu cần
        }
        throw new IllegalStateException("Không thể lấy userId từ JWT token");
    }

    public static String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getSubject();
            }
        } catch (Exception e) {
            // Log error nếu cần
        }
        return null;
    }
}

