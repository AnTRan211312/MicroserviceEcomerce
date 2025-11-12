package com.ecomerce.productservice.controller;

import com.ecomerce.productservice.dto.response.ApiResponse;
import com.ecomerce.productservice.dto.response.PageResponseDto;
import com.ecomerce.productservice.dto.response.ProductDetailResponse;
import com.ecomerce.productservice.dto.response.ProductSummaryResponse;
import com.ecomerce.productservice.model.Product;
import com.ecomerce.productservice.service.ProductService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product", description = "Quản lý sản phẩm")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ==================== PUBLIC ENDPOINTS (Summary - chỉ ảnh + tên + giá) ====================

    /**
     * Lấy danh sách sản phẩm (PUBLIC - GET không cần JWT)
     * Trả về ProductSummaryResponse - chỉ ảnh, tên, giá (cho trang web)
     * Hỗ trợ filtering qua query string: ?active=true&price>1000&category.id=1
     * Hỗ trợ pagination: ?page=0&size=20&sort=price,asc
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm", 
               description = "Public endpoint - trả về summary (ảnh + tên + giá). Hỗ trợ filtering và pagination qua query string. Ví dụ: ?active=true&price>1000&page=0&size=20&sort=price,asc")
    public ResponseEntity<ApiResponse<PageResponseDto<ProductSummaryResponse>>> getAllProducts(
            @Filter Specification<Product> spec,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDto<ProductSummaryResponse> products = productService.getAllProducts(spec, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách sản phẩm thành công", null, products));
    }

    /**
     * Tìm kiếm sản phẩm (PUBLIC - GET không cần JWT)
     * Trả về ProductSummaryResponse - chỉ ảnh, tên, giá
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sản phẩm", 
               description = "Public endpoint - trả về summary (ảnh + tên + giá). Hỗ trợ pagination: ?keyword=iPhone&page=0&size=20")
    public ResponseEntity<ApiResponse<PageResponseDto<ProductSummaryResponse>>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDto<ProductSummaryResponse> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Tìm kiếm sản phẩm thành công", null, products));
    }

    /**
     * Lấy sản phẩm theo category (PUBLIC - GET không cần JWT)
     * Trả về ProductSummaryResponse - chỉ ảnh, tên, giá
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lấy sản phẩm theo danh mục", 
               description = "Public endpoint - trả về summary (ảnh + tên + giá). Hỗ trợ pagination: ?page=0&size=20")
    public ResponseEntity<ApiResponse<PageResponseDto<ProductSummaryResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDto<ProductSummaryResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Lấy sản phẩm theo danh mục thành công", null, products));
    }

    /**
     * Lấy featured products (PUBLIC - GET không cần JWT)
     * Trả về ProductSummaryResponse - chỉ ảnh, tên, giá
     */
    @GetMapping("/featured")
    @Operation(summary = "Lấy sản phẩm nổi bật", 
               description = "Public endpoint - trả về summary (ảnh + tên + giá). Hỗ trợ pagination: ?page=0&size=20")
    public ResponseEntity<ApiResponse<PageResponseDto<ProductSummaryResponse>>> getFeaturedProducts(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponseDto<ProductSummaryResponse> products = productService.getFeaturedProducts(pageable);
        return ResponseEntity.ok(new ApiResponse<>("Lấy sản phẩm nổi bật thành công", null, products));
    }

    // ==================== PUBLIC ENDPOINTS (Detail - đầy đủ thông tin) ====================

    /**
     * Lấy sản phẩm theo ID (PUBLIC - GET không cần JWT)
     * Trả về ProductDetailResponse - đầy đủ thông tin (cho trang chi tiết)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo ID", description = "Public endpoint - trả về detail (đầy đủ thông tin)")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        ProductDetailResponse product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy sản phẩm thành công", null, product));
    }

    /**
     * Lấy sản phẩm theo slug (PUBLIC - GET không cần JWT)
     * Trả về ProductDetailResponse - đầy đủ thông tin (cho trang chi tiết)
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo slug", description = "Public endpoint - trả về detail (đầy đủ thông tin)")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        ProductDetailResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(new ApiResponse<>("Lấy sản phẩm thành công", null, product));
    }

}
