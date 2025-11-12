package com.ecomerce.orderservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

/**
 * Utility class để lấy thông tin từ JWT token
 */
public class JwtUtil {

    /**
     * Lấy userId từ JWT token
     * JWT có claim "user" với Map chứa "id"
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                // Lấy claim "user" (Map)
                Map<String, Object> userClaim = jwt.getClaim("user");
                if (userClaim != null && userClaim.containsKey("id")) {
                    Object id = userClaim.get("id");
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                    // Nếu là String, convert sang Long
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

    /**
     * Lấy email từ JWT token (subject)
     */
    public static String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getSubject(); // Subject là email
            }
        } catch (Exception e) {
            // Log error nếu cần
        }
        return null;
    }
}

