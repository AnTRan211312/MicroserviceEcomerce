package com.ecomerce.cartservice.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Configuration cho Jackson ObjectMapper
 * Đảm bảo encoding UTF-8 cho JSON response
 * Format BigDecimal dưới dạng số thông thường (không dùng scientific notation)
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // Custom serializer cho BigDecimal để tránh scientific notation
        SimpleModule bigDecimalModule = new SimpleModule();
        bigDecimalModule.addSerializer(BigDecimal.class, new JsonSerializer<BigDecimal>() {
            @Override
            public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                // Serialize dưới dạng số thông thường (plain number), không dùng scientific notation
                // Sử dụng writeRawValue để viết giá trị số dưới dạng plain string
                gen.writeRawValue(value.toPlainString());
            }
        });

        ObjectMapper mapper = builder
                .modules(new JavaTimeModule(), bigDecimalModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        // Đảm bảo encoding UTF-8
        mapper.getFactory().setCharacterEscapes(null);

        return mapper;
    }
}

