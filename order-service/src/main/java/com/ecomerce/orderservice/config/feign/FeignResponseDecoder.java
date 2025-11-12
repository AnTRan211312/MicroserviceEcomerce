package com.ecomerce.orderservice.config.feign;

import com.ecomerce.orderservice.client.dto.InventoryResponse;
import com.ecomerce.orderservice.client.dto.ProductDetailResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignResponseDecoder implements Decoder {

    private final Decoder delegate;
    private final ObjectMapper objectMapper;

    public FeignResponseDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.delegate = new SpringDecoder(messageConverters);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        String typeName = type.getTypeName();
        
        // Xử lý ProductDetailResponse từ product-service
        if (typeName.contains("ProductDetailResponse")) {
            return decodeApiResponse(response, type, ProductDetailResponse.class, "product-service");
        }
        
        // Xử lý InventoryResponse từ inventory-service
        if (typeName.contains("InventoryResponse")) {
            return decodeApiResponse(response, type, InventoryResponse.class, "inventory-service");
        }
        
        // For other types, use default decoder
        return delegate.decode(response, type);
    }
    
    private <T> T decodeApiResponse(Response response, Type type, Class<T> targetClass, String serviceName) throws IOException {
        try {
            byte[] bodyBytes = response.body().asInputStream().readAllBytes();
            String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
            
            log.debug("Raw response from {}: {}", serviceName, bodyString);
            
            ApiResponseWrapper apiResponse = objectMapper.readValue(bodyString, ApiResponseWrapper.class);
            
            if (apiResponse.getData() == null) {
                log.warn("⚠️ ApiResponse data is null from {}", serviceName);
                return null;
            }
            
            String dataJson = objectMapper.writeValueAsString(apiResponse.getData());
            T result = objectMapper.readValue(dataJson, targetClass);
            
            log.debug("✅ Unwrapped {} from {} - {}", targetClass.getSimpleName(), serviceName, result);
            
            return result;
        } catch (Exception e) {
            log.error("❌ Error decoding ApiResponse from {}: {}", serviceName, e.getMessage(), e);
            throw new IOException("Failed to decode ApiResponse from " + serviceName + ": " + e.getMessage(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponseWrapper {
        private String message;
        private String errorCode;
        private Object data;

        public Object getData() {
            return data;
        }
        
        @SuppressWarnings("unused")
        public String getMessage() {
            return message;
        }
        
        @SuppressWarnings("unused")
        public void setMessage(String message) {
            this.message = message;
        }
        
        @SuppressWarnings("unused")
        public String getErrorCode() {
            return errorCode;
        }
        
        @SuppressWarnings("unused")
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
        
        @SuppressWarnings("unused")
        public void setData(Object data) {
            this.data = data;
        }
    }
}

