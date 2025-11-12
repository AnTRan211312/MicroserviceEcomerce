package com.ecomerce.orderservice.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class FeignConfig {

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Luôn thêm Gateway Secret
                template.header("X-Gateway-Secret", gatewaySecret);

                // Thêm JWT token nếu có (từ SecurityContext)
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                        Jwt jwt = (Jwt) authentication.getPrincipal();
                        String token = jwt.getTokenValue();
                        template.header("Authorization", "Bearer " + token);
                        log.debug("✅ Added JWT token to Feign request: {}", template.url());
                    } else {
                        log.debug("⚠️ No JWT token found in SecurityContext for Feign request: {}", template.url());
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Failed to add JWT token to Feign request: {}", e.getMessage());
                }
            }
        };
    }

    @Bean
    public Decoder feignDecoder() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(converters);
        return new FeignResponseDecoder(objectFactory);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}

