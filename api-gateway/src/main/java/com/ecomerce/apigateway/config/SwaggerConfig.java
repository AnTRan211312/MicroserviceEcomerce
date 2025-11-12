package com.ecomerce.apigateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for API Gateway
 * Cấu hình Swagger với JWT authentication support
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setOpenapi("3.1.0");
        openAPI.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        openAPI.setComponents(new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token from /api/auth/login endpoint")
                )
        );
        openAPI.setInfo(new Info()
                .title("Ecommerce Microservices API Gateway")
                .version("1.0.0")
                .description("API Gateway for Ecommerce Microservices - Aggregated Swagger Documentation"));
        openAPI.setServers(List.of(
                new Server().url("http://localhost:8080").description("API Gateway Server")
        ));
        return openAPI;
    }
}
