package com.ecomerce.productservice.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter để đảm bảo tất cả response đều có charset=UTF-8
 */
@Component
@Order(1)
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set request encoding
        httpRequest.setCharacterEncoding("UTF-8");

        // Set response encoding
        httpResponse.setCharacterEncoding("UTF-8");

        // Đảm bảo Content-Type có charset=UTF-8 nếu là JSON
        String contentType = httpResponse.getContentType();
        if (contentType != null && contentType.contains("application/json") && !contentType.contains("charset")) {
            httpResponse.setContentType(contentType + ";charset=UTF-8");
        }

        chain.doFilter(request, response);
    }
}

