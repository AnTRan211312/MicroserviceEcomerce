package com.ecomerce.apigateway.config;

import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * JWT Configuration for API Gateway
 * Cấu hình JWT decoder để validate tokens từ auth-service
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    public static final MacAlgorithm MAC_ALGORITHM = MacAlgorithm.HS256;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey secretKeyObj = getSecretKey();
        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKeyObj)
                .macAlgorithm(MAC_ALGORITHM)
                .build();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(secretKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, MAC_ALGORITHM.getName());
    }
}

