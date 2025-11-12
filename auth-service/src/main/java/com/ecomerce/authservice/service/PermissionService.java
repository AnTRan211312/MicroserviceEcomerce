package com.ecomerce.authservice.service;

import com.ecomerce.authservice.dto.request.permission.PermissionRequestDto;
import com.ecomerce.authservice.dto.response.permission.PermissionResponseDto;
import com.ecomerce.authservice.model.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PermissionService {
    Page<PermissionResponseDto> findAllPermission(Specification<Permission> spec, Pageable pageable);
    PermissionResponseDto savePermission(PermissionRequestDto permissionRequestDto);
    PermissionResponseDto updatePermission(Long id,PermissionRequestDto permissionRequestDto);
    PermissionResponseDto deletePermission(Long id);

}
