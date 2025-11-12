package com.ecomerce.authservice.controller;


import com.ecomerce.authservice.annontaton.ApiMessage;
import com.ecomerce.authservice.dto.request.permission.PermissionRequestDto;
import com.ecomerce.authservice.dto.response.PageResponseDto;
import com.ecomerce.authservice.dto.response.permission.PermissionResponseDto;
import com.ecomerce.authservice.model.Permission;
import com.ecomerce.authservice.service.PermissionService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Permission")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('GET /api/permissions/*')")
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    @ApiMessage(value = "Tạo quyền hạn")
    @Operation(
            summary = "Tạo quyền hạn",
            description = "Yêu cầu quyền: <b>'GET /api/permissions/*</b>"
    )
    public PermissionResponseDto savePermission(
            @Valid @RequestBody PermissionRequestDto defaultPermissionRequestDto
    ) {
        return permissionService.savePermission(defaultPermissionRequestDto);
    }


    @GetMapping
    @ApiMessage("Lấy danh sách quyền hạn")
    @Operation(
            summary = "Lấy danh sách quyền hạn",
            description = "Yêu cầu quyền: <b>'GET /api/permissions/*</b>"
    )
    public ResponseEntity<?> findAllPermissions(
            @Filter Specification<Permission> spec,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<PermissionResponseDto> page = permissionService.findAllPermission(spec, pageable);

        PageResponseDto<PermissionResponseDto> res = new PageResponseDto<>(
                page.getContent(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        return ResponseEntity.ok(res);
    }

    @GetMapping("/all")
    @ApiMessage("Lấy toàn bộ quyền hạn (không phân trang)")
    @Operation(
            summary = "Lấy toàn bộ quyền hạn (không phân trang)",
            description = "Yêu cầu quyền: <b>'GET /api/permissions/*</b>"
    )
    public ResponseEntity<?> findAllPermissionsNoPaging(
            @Filter Specification<Permission> spec
    ) {
        List<PermissionResponseDto> list = permissionService
                .findAllPermission(spec, Pageable.unpaged())
                .getContent();

        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    @ApiMessage(value = "Cập nhật quyền hạn")
    @Operation(
            summary = "Cập nhật quyền hạn",
            description = "Yêu cầu quyền: <b>'GET /api/permissions/*</b>"
    )
    public PermissionResponseDto updatePermissionById(
            @Valid @RequestBody PermissionRequestDto defaultPermissionRequestDto,
            @PathVariable Long id
    ) {
        return permissionService.updatePermission(id, defaultPermissionRequestDto);
    }

    @DeleteMapping("/{id}")
    @ApiMessage(value = "Xóa quyền hạn")
    @Operation(
            summary = "Xóa quyền hạn",
            description = "Yêu cầu quyền: <b>'GET /api/permissions/*</b>"
    )
    public PermissionResponseDto deletePermissionById(
            @PathVariable Long id
    ) {
        return permissionService.deletePermission(id);
    }
}