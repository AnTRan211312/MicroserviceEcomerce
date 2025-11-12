package com.ecomerce.authservice.dto.request.role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDto {
    @NotBlank(message = "Tên chức vụ không được để trống")
    private String roleName;

    @NotBlank(message = "Mô tả không được để trống")
    private String roleDescription;

    private boolean active;

    @NotNull(message = "Danh sách quyền hạn không được để trống")
    @Valid
    private List<PermissionId> permissions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionId {
        @NotNull(message = "ID quyền hạn không được để trống")
        private Long id;
    }
}
