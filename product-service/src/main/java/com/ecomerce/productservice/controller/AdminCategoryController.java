package com.ecomerce.productservice.controller;

import com.ecomerce.productservice.dto.request.CategoryRequest;
import com.ecomerce.productservice.dto.response.ApiResponse;
import com.ecomerce.productservice.dto.response.CategoryAdminResponse;
import com.ecomerce.productservice.dto.response.PageResponseDto;
import com.ecomerce.productservice.model.Category;
import com.ecomerce.productservice.service.CategoryService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Category", description = "Quản lý danh mục (Admin)")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * Lấy tất cả danh mục cho admin (PROTECTED - cần permission GET /categories/admin)
     * Trả về CategoryAdminResponse - có thêm ngày giờ
     */
    @GetMapping
    @PreAuthorize("hasAuthority('GET /api/admin/categories')")
    @Operation(summary = "Lấy danh sách danh mục cho admin với filtering",
               description = "Yêu cầu quyền: <b>GET /api/admin/categories</b>. Hỗ trợ filtering qua query string. Ví dụ: ?name=iPhone&page=0&size=20&sort=id,desc")
    public ResponseEntity<ApiResponse<PageResponseDto<CategoryAdminResponse>>> getAllCategories(
            @Filter org.springframework.data.jpa.domain.Specification<Category> spec,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDto<CategoryAdminResponse> categories = categoryService.getAllCategoriesForAdmin(spec, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách danh mục thành công", null, categories));
    }

    /**
     * Lấy danh mục theo ID cho admin (PROTECTED - cần permission GET /categories/admin/{id})
     * Trả về CategoryAdminResponse - có thêm ngày giờ
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET /api/admin/categories/{id}')")
    @Operation(summary = "Lấy danh mục theo ID cho admin", description = "Yêu cầu quyền: <b>GET /api/admin/categories/{id}</b> - có thêm ngày giờ")
    public ResponseEntity<ApiResponse<CategoryAdminResponse>> getCategoryById(@PathVariable Long id) {
        CategoryAdminResponse category = categoryService.getCategoryByIdForAdmin(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh mục thành công", null, category));
    }

    /**
     * Tạo danh mục mới (PROTECTED - cần permission POST /categories)
     * Trả về CategoryAdminResponse - có thêm ngày giờ
     */
    @PostMapping
    @PreAuthorize("hasAuthority('POST /api/admin/categories')")
    @Operation(summary = "Tạo danh mục mới", description = "Yêu cầu quyền: <b>POST /api/admin/categories</b> - trả về admin response")
    public ResponseEntity<ApiResponse<CategoryAdminResponse>> createCategory(
            @RequestBody @Valid CategoryRequest request // <--- Thay thế bằng @RequestBody và DTO
    ) {
        CategoryAdminResponse category = categoryService.createCategory(request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Tạo danh mục thành công", null, category));
    }

    /**
     * Cập nhật danh mục (PROTECTED - cần permission PUT /categories)
     * Trả về CategoryAdminResponse - có thêm ngày giờ
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PUT /api/admin/categories/{id}')")
    @Operation(summary = "Cập nhật danh mục", description = "Yêu cầu quyền: <b>PUT /api/admin/categories/{id}</b> - trả về admin response")
    public ResponseEntity<ApiResponse<CategoryAdminResponse>> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequest request // <--- SỬA TẠI ĐÂY
    ) {
        // Gọi service với dữ liệu từ đối tượng request
        CategoryAdminResponse category = categoryService.updateCategory(
                id,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.ok(new ApiResponse<>("Cập nhật danh mục thành công", null, category));
    }
    /**
     * Xóa danh mục (PROTECTED - cần permission DELETE /categories/{id})
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE /api/admin/categories/{id}')")
    @Operation(summary = "Xóa danh mục", description = "Yêu cầu quyền: <b>DELETE /api/admin/categories/{id}</b>")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa danh mục thành công", null, null));
    }
}

