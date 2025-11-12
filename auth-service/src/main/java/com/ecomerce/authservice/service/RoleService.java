package com.ecomerce.authservice.service;

import com.ecomerce.authservice.dto.request.role.RoleRequestDto;
import com.ecomerce.authservice.dto.response.role.RoleResponseDto;
import com.ecomerce.authservice.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface RoleService {
    RoleResponseDto saveRole(RoleRequestDto roleRequestDto);

    RoleResponseDto updateRole(Long id, RoleRequestDto roleRequestDto);

    Page<RoleResponseDto> findAllRoles(Specification<Role> spec, Pageable pageable);

    RoleResponseDto deleteRoleById(Long id);
}
