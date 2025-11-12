package com.ecomerce.cartservice.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // BẮT BUỘC để @PreAuthorize hoạt động
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewaySecretFilter gatewaySecretFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final SkipPathBearTokenResolver skipPathBearTokenResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 1. Luôn thêm GatewaySecretFilter để kiểm tra secret từ Gateway ĐẦU TIÊN
                .addFilterBefore(gatewaySecretFilter, UsernamePasswordAuthenticationFilter.class)

                // 2. Tắt các tính năng không cần thiết cho microservice
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3. Cấu hình quy tắc cho các request
                .authorizeHttpRequests(auth -> auth
                        // --- QUY TẮC CHO SWAGGER & MONITORING ---
                        // Các đường dẫn này không phân biệt phương thức
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        // Tất cả các request còn lại ĐỀU PHẢI được xác thực (có JWT hợp lệ)
                        .anyRequest().authenticated()
                )

                // 4. Cấu hình OAuth2 Resource Server để giải mã và xác thực JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> {}) // Sử dụng cấu hình JwtDecoder mặc định đã @Bean
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // Dùng EntryPoint tùy chỉnh để trả về lỗi 401 đẹp hơn
                        .bearerTokenResolver(skipPathBearTokenResolver) // Skip JWT cho public endpoints
                )

                // 5. Đảm bảo service là STATELESS, không lưu session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return httpSecurity.build();
    }
}

