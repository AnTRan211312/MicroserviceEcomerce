package com.ecomerce.authservice.controller;

import com.ecomerce.authservice.dto.response.user.InternalUserResponseDto;
import com.ecomerce.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal Controller cho inter-service communication
 * Chỉ được gọi bởi các service khác qua Feign Client
 * Yêu cầu Gateway Secret để authenticate
 */
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    /**
     * Lấy thông tin user theo ID (internal call)
     * Chỉ cần Gateway Secret, không cần JWT
     * Chỉ trả về các thông tin cần thiết: id, name, email, role
     * Không bao gồm thông tin nhạy cảm như dob, address, gender, logoUrl
     */
    @GetMapping("/{userId}")
    public ResponseEntity<InternalUserResponseDto> getUserById(@PathVariable Long userId) {
        InternalUserResponseDto user = userService.findUserByIdForInternal(userId);
        return ResponseEntity.ok(user);
    }


}

