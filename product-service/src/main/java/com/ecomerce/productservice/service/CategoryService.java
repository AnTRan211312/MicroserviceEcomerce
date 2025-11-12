package com.ecomerce.productservice.service;

import com.ecomerce.productservice.dto.CategoryInfo;
import com.ecomerce.productservice.dto.response.CategoryAdminResponse;
import com.ecomerce.productservice.dto.response.PageResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    
    // ==================== PUBLIC ENDPOINTS ====================
    
    /**
     * Lấy tất cả categories (public)
     */
    List<CategoryInfo> getAllCategories();
    
    /**
     * Lấy category theo ID (public)
     */
    CategoryInfo getCategoryById(Long id);
    
    /**
     * Lấy category theo slug (public)
     */
    CategoryInfo getCategoryBySlug(String slug);
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Lấy tất cả categories cho admin (có thêm ngày giờ)
     */
    List<CategoryAdminResponse> getAllCategoriesForAdmin();
    
    /**
     * Lấy danh sách categories cho admin với Specification filter và pagination (có thêm ngày giờ)
     */
    PageResponseDto<CategoryAdminResponse> getAllCategoriesForAdmin(
            org.springframework.data.jpa.domain.Specification<com.ecomerce.productservice.model.Category> spec,
            Pageable pageable
    );
    
    /**
     * Lấy category theo ID cho admin (có thêm ngày giờ)
     */
    CategoryAdminResponse getCategoryByIdForAdmin(Long id);
    
    /**
     * Tạo category mới (trả về admin response)
     */
    CategoryAdminResponse createCategory(String name, String description);
    
    /**
     * Cập nhật category (trả về admin response)
     */
    CategoryAdminResponse updateCategory(Long id, String name, String description);
    
    /**
     * Xóa category
     */
    void deleteCategory(Long id);
}

