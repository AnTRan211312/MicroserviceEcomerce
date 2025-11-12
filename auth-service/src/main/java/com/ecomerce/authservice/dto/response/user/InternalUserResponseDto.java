package com.ecomerce.authservice.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho internal calls (inter-service communication)
 * Chỉ chứa các thông tin cần thiết: id, name, email, role
 * Giảm bandwidth và tăng bảo mật bằng cách không expose thông tin nhạy cảm
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InternalUserResponseDto {
    private Long id;
    private String name;
    private String email;
    private RoleInfo role;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class RoleInfo {
        private Long id;
        private String name;
        private String description;
    }
}

