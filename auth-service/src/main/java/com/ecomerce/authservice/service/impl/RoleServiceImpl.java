package com.ecomerce.authservice.service.impl;

import com.ecomerce.authservice.dto.request.role.RoleRequestDto;
import com.ecomerce.authservice.dto.response.role.RoleResponseDto;
import com.ecomerce.authservice.model.Permission;
import com.ecomerce.authservice.model.Role;
import com.ecomerce.authservice.repository.PermissionRepository;
import com.ecomerce.authservice.repository.RoleRepository;
import com.ecomerce.authservice.repository.UserRepository;
import com.ecomerce.authservice.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    @Override
    public RoleResponseDto saveRole(RoleRequestDto roleRequestDto) {
        Role role = new Role(
                roleRequestDto.getRoleName(),
                roleRequestDto.getRoleDescription()
        );
        Set<Permission> permissions = null;
        if(roleRequestDto.getPermissions() != null){
            Set<Long> permissionIds = roleRequestDto
                    .getPermissions()
                    .stream()
                    .map(RoleRequestDto.PermissionId::getId)
                    .collect(Collectors.toSet());
            permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            if(permissions.size() != roleRequestDto.getPermissions().size()){
                throw new EntityNotFoundException("Quyền hạn không toonf tại");
            }
        }
        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);
        return mapToRoleResponseDto(savedRole);

    }

    @Override
    public RoleResponseDto updateRole(Long id, RoleRequestDto roleRequestDto) {

        Role role  = roleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ch?c v? kh�ng t?n t?i"));
        String currentName = role.getRoleName();
        if(currentName != null &&
        !currentName.equalsIgnoreCase("ADMIN")
        && !currentName.equalsIgnoreCase("USER"))
            role.setRoleName(roleRequestDto.getRoleName());
        role.setRoleDescription(roleRequestDto.getRoleDescription());
        role.setActive(roleRequestDto.isActive());
        if(roleRequestDto.getPermissions() != null){
            Set<Long> requestedPermissionIds = roleRequestDto.getPermissions().stream()
                    .map(RoleRequestDto.PermissionId::getId)
                    .collect(Collectors.toSet());
            Set<Permission> currentPermissions = new HashSet<>(role.getPermissions());
            currentPermissions.removeIf(x -> !requestedPermissionIds.contains(x.getId()));

            Set<Long> currentPermissionIds = currentPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());

            Set<Long> newPermissionIdsToAdd = new HashSet<>(requestedPermissionIds);
            requestedPermissionIds.removeAll(currentPermissionIds);

            if (!newPermissionIdsToAdd.isEmpty()) {
                List<Permission> newPermissions = permissionRepository.findAllById(newPermissionIdsToAdd);
                currentPermissions.addAll(newPermissions);
            }

            role.setPermissions(currentPermissions);
        }
        Role updatedRole = roleRepository.saveAndFlush(role);
        return mapToRoleResponseDto(updatedRole);
    }

    @Override
    public Page<RoleResponseDto> findAllRoles(Specification<Role> spec, Pageable pageable) {
        return roleRepository
                .findAll(spec, pageable)
                .map(this::mapToRoleResponseDto);
    }

    @Override
    public RoleResponseDto deleteRoleById(Long id) {
        Role role = roleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chức vụ không tồn tại"));

        String currentName = role.getRoleName();
        if (
                currentName != null
                        && !currentName.equalsIgnoreCase("ADMIN")
                        && !currentName.equalsIgnoreCase("USER")
        ) {
            RoleResponseDto defaultRoleResponseDto = mapToRoleResponseDto(role);

            if (role.getPermissions() != null) role.getPermissions().clear();
            userRepository.detachUsersFromRole(role.getId());

            roleRepository.delete(role);
            return defaultRoleResponseDto;
        }

        throw new AccessDeniedException("Không thể xóa chức vụ");
    }

    private RoleResponseDto mapToRoleResponseDto(Role role) {
        RoleResponseDto roleResponseDto = new RoleResponseDto(
                role.getId(),
                role.isActive(),
                role.getRoleName(),
                role.getCreatedAt().toString(),
                role.getUpdatedAt().toString(),
                role.getRoleDescription()
        );
        List<RoleResponseDto.Permission> permissions = role.getPermissions()
                .stream()
                .map(p -> new RoleResponseDto.Permission(
                        p.getId(),
                        p.getName(),
                        p.getApiPath(),
                        p.getMethod(),
                        p.getModule()
                ))
                .toList();
        roleResponseDto.setPermissions(permissions);

        return roleResponseDto;
    }
}
