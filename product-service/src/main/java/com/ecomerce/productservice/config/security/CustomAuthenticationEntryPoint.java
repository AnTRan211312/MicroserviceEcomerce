package com.ecomerce.productservice.config.security;

import com.ecomerce.productservice.dto.response.ApiResponse; // Nhớ đổi package cho đúng
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Vẫn giữ lại delegate để xử lý việc set status 401 và header WWW-Authenticate đúng chuẩn
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        // Bất kỳ khi nào EntryPoint này được gọi, nó đều có nghĩa là đã có lỗi xác thực
        // trên một endpoint YÊU CẦU xác thực.
        // Vì vậy, ta luôn trả về lỗi 401 tùy chỉnh.

        // 1. Gọi delegate để nó set HTTP Status Code 401 và các header cần thiết.
        delegate.commence(request, response, authException);

        // 2. Tùy chỉnh lại body của response để thân thiện hơn.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = new ApiResponse<>(
                "Yêu cầu xác thực không hợp lệ. Vui lòng kiểm tra lại token.",
                "UNAUTHORIZED"
        );

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}