package com.ecomerce.authservice.controller;

import com.ecomerce.authservice.annontaton.ApiMessage;
import com.ecomerce.authservice.dto.request.role.RoleRequestDto;
import com.ecomerce.authservice.dto.response.PageResponseDto;
import com.ecomerce.authservice.dto.response.role.RoleResponseDto;
import com.ecomerce.authservice.model.Role;
import com.ecomerce.authservice.service.RoleService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Role")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @ApiMessage(value = "Tạo role")
    @PreAuthorize("hasAuthority('POST /api/roles')")
    @Operation(
            summary = "Tạo role",
            description = "Yêu cầu quyền: <b>POST /api/roles</b>"
    )
    public ResponseEntity<RoleResponseDto> saveRole(
            @Valid @RequestBody RoleRequestDto defaultRoleRequestDto
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.saveRole(defaultRoleRequestDto));
    }

    @GetMapping
    @ApiMessage(value = "Lấy danh sách role")
    @PreAuthorize("hasAuthority('GET /api/roles')")
    @Operation(
            summary = "Lấy danh sách role",
            description = "Yêu cầu quyền : <b> GET /api/roles</b>"
    )
    public ResponseEntity<PageResponseDto<RoleResponseDto>> findAllRoles(
            @Filter Specification<Role> spec,
            @PageableDefault(size = 5) Pageable pageable
    ){
        Page<RoleResponseDto> page = roleService.findAllRoles(spec, pageable);
        PageResponseDto<RoleResponseDto> res = new PageResponseDto<>(
                page.getContent(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return ResponseEntity.ok(res);
    }


    @PutMapping({"/{id}"})
    @ApiMessage(value = "Cập nhật Role")
    @PreAuthorize("hasAuthority('PUT /api/roles/{id}')")
    @Operation(
            summary = "Cập nhật Role",
            description = "Yêu cầu quyền: <b>PUT /api/roles/{id}</b>"
    )
    public ResponseEntity<RoleResponseDto> updateRoleById(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequestDto defaultRoleRequestDto
    ) {
        return ResponseEntity.ok(roleService.updateRole(id, defaultRoleRequestDto));
    }
    @DeleteMapping("/{id}")
    @ApiMessage(value = "Xóa Role theo id")
    @PreAuthorize("hasAuthority('DELETE /api/roles/{id}')")
    @Operation(
            summary = "Xóa Role theo id",
            description = "Yêu cầu quyền: <b>DELETE /api/roles/{id}</b>"
    )
    public ResponseEntity<RoleResponseDto> deleteRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.deleteRoleById(id));
    }

}