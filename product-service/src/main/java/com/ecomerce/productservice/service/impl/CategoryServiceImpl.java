package com.ecomerce.productservice.service.impl;

import com.ecomerce.productservice.advice.exeption.ResourceAlreadyExistsException;
import com.ecomerce.productservice.advice.exeption.ResourceNotFoundException;
import com.ecomerce.productservice.dto.CategoryInfo;
import com.ecomerce.productservice.dto.response.CategoryAdminResponse;
import com.ecomerce.productservice.dto.response.PageResponseDto;
import com.ecomerce.productservice.model.Category;
import com.ecomerce.productservice.repository.CategoryRepository;
import com.ecomerce.productservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPLICATE_DASHES = Pattern.compile("-+");

    // ==================== PUBLIC ENDPOINTS ====================

    @Override
    @Transactional(readOnly = true)
    public List<CategoryInfo> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToCategoryInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryInfo getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        
        return convertToCategoryInfo(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryInfo getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với slug: " + slug));
        
        return convertToCategoryInfo(category);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Override
    @Transactional(readOnly = true)
    public List<CategoryAdminResponse> getAllCategoriesForAdmin() {
        return categoryRepository.findAll().stream()
                .map(this::convertToAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CategoryAdminResponse> getAllCategoriesForAdmin(
            org.springframework.data.jpa.domain.Specification<Category> spec,
            Pageable pageable
    ) {
        // Sử dụng repository với Specification
        org.springframework.data.domain.Page<Category> categoryPage = categoryRepository.findAllWithFilter(spec, pageable);
        
        PageResponseDto<CategoryAdminResponse> response = new PageResponseDto<>();
        response.setContent(categoryPage.getContent().stream()
                .map(this::convertToAdminResponse)
                .toList());
        response.setPage(categoryPage.getNumber() + 1); // 1-indexed
        response.setSize(categoryPage.getSize());
        response.setTotalElements(categoryPage.getTotalElements());
        response.setTotalPages(categoryPage.getTotalPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryAdminResponse getCategoryByIdForAdmin(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        
        return convertToAdminResponse(category);
    }

    @Override
    @Transactional
    public CategoryAdminResponse createCategory(String name, String description) {
        // Tạo slug từ tên
        String slug = generateSlug(name);
        
        // Kiểm tra slug đã tồn tại chưa
        if (categoryRepository.existsBySlug(slug)) {
            throw new ResourceAlreadyExistsException("Slug '" + slug + "' đã tồn tại");
        }
        
        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Đã tạo danh mục mới: {}", savedCategory.getId());
        
        return convertToAdminResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryAdminResponse updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        
        // Tạo slug từ tên mới
        String newSlug = generateSlug(name);
        
        // Kiểm tra slug đã tồn tại chưa (loại trừ category hiện tại)
        if (!category.getSlug().equals(newSlug) && categoryRepository.existsBySlugAndIdNot(newSlug, id)) {
            throw new ResourceAlreadyExistsException("Slug '" + newSlug + "' đã tồn tại");
        }
        
        category.setName(name);
        category.setSlug(newSlug);
        category.setDescription(description);
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Đã cập nhật danh mục: {}", updatedCategory.getId());
        
        return convertToAdminResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        
        // Kiểm tra xem category có products không
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalStateException("Không thể xóa danh mục vì còn sản phẩm trong danh mục này");
        }
        
        categoryRepository.delete(category);
        log.info("Đã xóa danh mục: {}", id);
    }

    // ==================== CONVERTER METHODS ====================

    /**
     * Chuyển đổi Category thành CategoryInfo (public)
     */
    private CategoryInfo convertToCategoryInfo(Category category) {
        return CategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }

    /**
     * Chuyển đổi Category thành CategoryAdminResponse (admin - có thêm ngày giờ)
     */
    private CategoryAdminResponse convertToAdminResponse(Category category) {
        return CategoryAdminResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Tạo slug từ tên (Vietnamese friendly)
     * Ví dụ: "Thiết Bị Điện Tử" -> "thiet-bi-dien-tu"
     */
    private String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        
        // 1. Normalize để tách dấu tiếng Việt (NFD = Canonical Decomposition)
        // Ví dụ: "ế" -> "e" + "́", "ị" -> "i" + "̣"
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        
        // 2. Loại bỏ tất cả các ký tự không phải ASCII (bao gồm dấu tiếng Việt)
        // Giữ lại: a-z, A-Z, 0-9, khoảng trắng, dấu gạch ngang
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        
        // 3. Chuyển "Đ" thành "D" (vì "Đ" không phải ASCII)
        slug = slug.replace("Đ", "D").replace("đ", "d");
        
        // 4. Thay thế khoảng trắng và các ký tự đặc biệt bằng dấu gạch ngang
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("[^a-zA-Z0-9-]", "-");
        
        // 5. Chuyển về chữ thường
        slug = slug.toLowerCase(Locale.ENGLISH);
        
        // 6. Loại bỏ các dấu gạch ngang liên tiếp (ví dụ: "---" -> "-")
        slug = DUPLICATE_DASHES.matcher(slug).replaceAll("-");
        
        // 7. Loại bỏ dấu gạch ngang ở đầu và cuối
        slug = slug.replaceAll("^-|-$", "");
        
        return slug;
    }
}
