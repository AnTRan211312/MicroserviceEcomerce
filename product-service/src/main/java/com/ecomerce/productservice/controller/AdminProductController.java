package com.ecomerce.productservice.controller;

import com.ecomerce.productservice.dto.request.ProductCreateRequest;
import com.ecomerce.productservice.dto.request.ProductUpdateRequest;
import com.ecomerce.productservice.dto.response.ApiResponse;
import com.ecomerce.productservice.dto.response.PageResponseDto;
import com.ecomerce.productservice.dto.response.ProductAdminResponse;
import com.ecomerce.productservice.dto.response.ProductListResponse;
import com.ecomerce.productservice.model.Product;
import com.ecomerce.productservice.service.ProductService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin Product", description = "Quản lý sản phẩm (Admin)")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAuthority('GET /api/admin/products')")
    @Operation(summary = "Lấy danh sách sản phẩm (admin)", 
               description = "Trả về ProductListResponse - tối giản. Hỗ trợ filtering: ?name=iPhone&active=true&page=0&size=20")
    public ResponseEntity<ApiResponse<PageResponseDto<ProductListResponse>>> getAllProducts(
            @Filter Specification<Product> spec,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách sản phẩm thành công", null, 
                productService.getAllProductsForAdminList(spec, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET /api/admin/products/{id}')")
    @Operation(summary = "Lấy chi tiết sản phẩm (admin)")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Lấy sản phẩm thành công", null, 
                productService.getProductByIdForAdmin(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('POST /api/admin/products')")
    @Operation(summary = "Tạo sản phẩm mới", 
               description = "Nhận JSON với thumbnailUrl và imageUrls (từ upload riêng)")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Tạo sản phẩm thành công", null, productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PUT /api/admin/products/{id}')")
    @Operation(summary = "Cập nhật sản phẩm (full update)", 
               description = "Tất cả fields optional. Nhận JSON với thumbnailUrl và imageUrls")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Cập nhật sản phẩm thành công", null, 
                productService.updateProduct(id, request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PATCH /api/admin/products/{id}')")
    @Operation(summary = "Partial update sản phẩm", 
               description = "Chỉ update fields được gửi lên. Ví dụ: {\"price\": 1000}")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Cập nhật sản phẩm thành công", null, 
                productService.patchProduct(id, updates)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE /api/admin/products/{id}')")
    @Operation(summary = "Xóa sản phẩm")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa sản phẩm thành công", null, null));
    }
    
    @PostMapping("/upload-image")
    @PreAuthorize("hasAuthority('POST /api/admin/products/upload-image')")
    @Operation(summary = "Upload ảnh đơn lẻ", 
               description = "Upload thumbnail. Trả về URL để dùng trong ProductCreateRequest/UpdateRequest")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(required = false) Long productId
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Upload ảnh thành công", null, 
                productService.uploadImage(imageFile, productId)));
    }
    
    @PostMapping("/upload-images")
    @PreAuthorize("hasAuthority('POST /api/admin/products/upload-images')")
    @Operation(summary = "Upload nhiều ảnh", 
               description = "Upload gallery images. Trả về danh sách URLs để dùng trong ProductCreateRequest/UpdateRequest")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @RequestParam("images") MultipartFile[] imageFiles,
            @RequestParam(required = false) Long productId
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Upload ảnh thành công", null, 
                productService.uploadImages(imageFiles, productId)));
    }
}

