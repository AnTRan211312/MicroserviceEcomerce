package com.ecomerce.authservice.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewaySecretFilter gatewaySecretFilter;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token", // Refresh token endpoint - không cần JWT, chỉ cần refresh_token cookie
            "/api/auth/forgot",
            "/api/auth/verify-otp",
            "/api/auth/reset",
            "/api/auth/resend-otp"
    };

    // Internal endpoints - chỉ cần Gateway Secret (cho inter-service communication)
    private static final String[] INTERNAL_ENDPOINTS = {
            "/api/internal/**"
    };

    private static final String[] MONITORING_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info"
    };

    private List<String> getAllPermittedEndpoints() {
        List<String> allPermitted = new ArrayList<>(Arrays.asList(PUBLIC_ENDPOINTS));
        allPermitted.addAll(Arrays.asList(MONITORING_ENDPOINTS));
        return allPermitted;
    }

    private boolean isPermittedEndpoint(String path, List<String> permittedPatterns) {
        return permittedPatterns.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(prefix);
            }
            return path.equals(pattern);
        });
    }

    private void applyCommonConfig(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(gatewaySecretFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(PUBLIC_ENDPOINTS)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        applyCommonConfig(http);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain internalSecurityFilterChain(HttpSecurity http) throws Exception {
        // Internal endpoints - chỉ cần Gateway Secret, không cần JWT
        http.securityMatcher(INTERNAL_ENDPOINTS)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        applyCommonConfig(http);
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain protectedSecurityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    ) throws Exception {

        List<String> permittedEndpoints = getAllPermittedEndpoints();

        http.securityMatcher(request -> {
                    String path = request.getRequestURI();
                    return !isPermittedEndpoint(path, permittedEndpoints);
                })
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .bearerTokenResolver(new SkipPathBearTokenResolver())
                );

        applyCommonConfig(http);
        return http.build();
    }
}

