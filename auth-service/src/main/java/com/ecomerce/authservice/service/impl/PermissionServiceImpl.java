package com.ecomerce.authservice.service.impl;

import com.ecomerce.authservice.dto.request.permission.PermissionRequestDto;
import com.ecomerce.authservice.dto.response.permission.PermissionResponseDto;
import com.ecomerce.authservice.model.Permission;
import com.ecomerce.authservice.repository.PermissionRepository;
import com.ecomerce.authservice.repository.RoleRepository;
import com.ecomerce.authservice.service.PermissionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    @Override
    public Page<PermissionResponseDto> findAllPermission(Specification<Permission> spec, Pageable pageable) {
        return permissionRepository
                .findAll(spec,pageable)
                .map(this::mapToDefaultResponseDto);
    }

    @Override
    public PermissionResponseDto savePermission(PermissionRequestDto permissionRequestDto) {
        Permission permission = new Permission(
                permissionRequestDto.getName(),
                permissionRequestDto.getApiPath(),
                permissionRequestDto.getMethod(),
                permissionRequestDto.getModule()
        );
        Permission savedPermission = permissionRepository.save(permission);
        return mapToDefaultResponseDto(savedPermission);
    }
    @Override
    public PermissionResponseDto updatePermission(Long id,PermissionRequestDto permissionRequestDto) {
        Permission permission = permissionRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("không tìm thấy quyền hạn này"));
        permission.setName(permissionRequestDto.getName());
        permission.setApiPath(permissionRequestDto.getApiPath());
        permission.setMethod(permissionRequestDto.getMethod());
        permission.setModule(permissionRequestDto.getModule());

        Permission savedPermission = permissionRepository.save(permission);
        return mapToDefaultResponseDto(savedPermission);
    }

    @Override
    public PermissionResponseDto deletePermission(Long id) {
        Permission permission = permissionRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quyền hạn này"));

        permission.getRoles().forEach(role -> {
            role.getPermissions().remove(permission);
            roleRepository.saveAndFlush(role);
        });

        permissionRepository.delete(permission);
        return mapToDefaultResponseDto(permission);
    }

    private PermissionResponseDto mapToDefaultResponseDto(Permission permission) {

        PermissionResponseDto res = new PermissionResponseDto(
                permission.getId(),
                permission.getName(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getCreatedAt().toString(),
                permission.getUpdatedAt().toString()
        );

        return res;
    }


}
