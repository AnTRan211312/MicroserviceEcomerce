package com.ecomerce.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VnpayConfig {
    private String tmnCode;
    private String hashSecret;
    private String paymentUrl;
    private String apiUrl;
    private String returnUrl;
    private String version;
    private String command;
    private String orderType;
    private String locale;
    private String currCode;
}

