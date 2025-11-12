package com.ecomerce.authservice.dto.response.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponseDto {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private String createdAt;
    private String updatedAt;
    private List<Permission> permissions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Permission {
        private Long id;
        private String name;
        private String apiPath;
        private String method;
        private String module;
    }

    public RoleResponseDto(
            Long id, boolean active, String name,
            String createdAt, String updatedAt, String description
    ) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.description = description;
    }
}
