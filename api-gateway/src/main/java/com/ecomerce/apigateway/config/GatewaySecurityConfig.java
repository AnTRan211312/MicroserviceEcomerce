package com.ecomerce.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for API Gateway (WebFlux)
 * Xử lý JWT validation tập trung tại Gateway
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private final ReactiveJwtDecoder reactiveJwtDecoder;

    /**
     * Public endpoints - Không cần JWT token
     * Bao gồm:
     * - Auth endpoints (đăng ký, đăng nhập, quên mật khẩu)
     * - Product endpoints (xem danh sách, chi tiết sản phẩm)
     * - Category endpoints (xem danh mục)
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot",
            "/api/auth/verify-otp",
            "/api/auth/reset",
            "/api/auth/resend-otp",
            // Product service - Public endpoints (chỉ GET)
            "/api/products/**",
            "/api/categories/**"
    };

    /**
     * Monitoring endpoints - Không cần JWT
     */
    private static final String[] MONITORING_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/actuator/health/**",
            "/actuator/info",
            "/eureka/**"
    };

    /**
     * Security filter chain duy nhất - sử dụng PublicEndpointAuthenticationFilter
     * để remove Authorization header cho public endpoints
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Monitoring endpoints - Không cần JWT
                        .pathMatchers(MONITORING_ENDPOINTS).permitAll()
                        
                        // Public endpoints - Cho phép cả anonymous và authenticated
                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
                        
                        // Tất cả các endpoints khác - Cần JWT (authenticated)
                        .anyExchange().authenticated()
                )
                // OAuth2 Resource Server - nhưng PublicEndpointAuthenticationFilter sẽ remove
                // Authorization header cho public endpoints, nên không có JWT để validate
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(reactiveJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint((exchange, ex) -> {
                            String path = exchange.getRequest().getURI().getPath();
                            
                            // Nếu là public endpoint, ignore lỗi (không trả về 401)
                            if (isPublicEndpoint(path)) {
                                log.debug("✅ Public endpoint - ignoring authentication error: {}", path);
                                return Mono.empty();
                            }
                            
                            // Protected endpoint - trả về 401
                            log.error("❌ Authentication failed for protected endpoint: {} - Exception: {}", 
                                    path, ex.getClass().getSimpleName());
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);
                            return response.setComplete();
                        })
                );

        log.info("✅ SecurityWebFilterChain configured - Using PublicEndpointAuthenticationFilter for public endpoints");
        return http.build();
    }

    private boolean isPublicEndpoint(String path) {
        // Kiểm tra public endpoints
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (pathMatches(path, publicEndpoint)) {
                return true;
            }
        }
        
        // Kiểm tra monitoring endpoints
        for (String monitoringEndpoint : MONITORING_ENDPOINTS) {
            if (pathMatches(path, monitoringEndpoint)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean pathMatches(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    /**
     * JWT Authentication Converter
     * Extract permissions từ JWT token claims
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            
            // Extract permissions từ claim "permissions"
            if (jwt.hasClaim("permissions")) {
                List<String> permissions = jwt.getClaimAsStringList("permissions");
                if (permissions != null) {
                    authorities.addAll(permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()));
                }
            }
            
            return authorities;
        });
        
        return jwt -> Mono.just(converter.convert(jwt));
    }
}
