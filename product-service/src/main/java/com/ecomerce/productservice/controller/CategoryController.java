package com.ecomerce.productservice.controller;

import com.ecomerce.productservice.dto.CategoryInfo;
import com.ecomerce.productservice.dto.response.ApiResponse;
import com.ecomerce.productservice.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "Quản lý danh mục")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Lấy tất cả danh mục (PUBLIC - GET không cần JWT)
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả danh mục", description = "Public endpoint - không cần JWT")
    public ResponseEntity<ApiResponse<List<CategoryInfo>>> getAllCategories() {
        List<CategoryInfo> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách danh mục thành công", null, categories));
    }

    /**
     * Lấy danh mục theo ID (PUBLIC - GET không cần JWT)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy danh mục theo ID", description = "Public endpoint - không cần JWT")
    public ResponseEntity<ApiResponse<CategoryInfo>> getCategoryById(@PathVariable Long id) {
        CategoryInfo category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh mục thành công", null, category));
    }

    /**
     * Lấy danh mục theo slug (PUBLIC - GET không cần JWT)
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Lấy danh mục theo slug", description = "Public endpoint - không cần JWT")
    public ResponseEntity<ApiResponse<CategoryInfo>> getCategoryBySlug(@PathVariable String slug) {
        CategoryInfo category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh mục thành công", null, category));
    }

}

