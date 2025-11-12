package com.ecomerce.paymentservice.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class VnpayUtil {
    
    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;
    
    public String createPaymentUrl(Map<String, String> params, String baseUrl) {
        StringBuilder queryString = new StringBuilder();
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                queryString.append("=");
                queryString.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                queryString.append("&");
            }
        }
        
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, queryString.toString());
        queryString.append("&vnp_SecureHash=").append(vnp_SecureHash);
        
        return baseUrl + "?" + queryString.toString();
    }
    
    public boolean validateCallback(Map<String, String> params) {
        String vnp_SecureHash = params.remove("vnp_SecureHash");
        
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }
        
        StringBuilder queryString = new StringBuilder();
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getKey().startsWith("vnp_") && entry.getValue() != null && !entry.getValue().isEmpty()) {
                queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                queryString.append("=");
                queryString.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                queryString.append("&");
            }
        }
        
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        
        String calculatedHash = hmacSHA512(vnp_HashSecret, queryString.toString());
        return calculatedHash.equals(vnp_SecureHash);
    }
    
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmacSHA512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmacSHA512.init(secretKey);
            byte[] hashBytes = hmacSHA512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating HMAC SHA512", e);
        }
    }
    
    public String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}

