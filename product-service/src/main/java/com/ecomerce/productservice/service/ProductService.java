package com.ecomerce.productservice.service;

import com.ecomerce.productservice.dto.request.ProductCreateRequest;
import com.ecomerce.productservice.dto.request.ProductUpdateRequest;
import com.ecomerce.productservice.dto.response.PageResponseDto;
import com.ecomerce.productservice.dto.response.ProductAdminResponse;
import com.ecomerce.productservice.dto.response.ProductDetailResponse;
import com.ecomerce.productservice.dto.response.ProductListResponse;
import com.ecomerce.productservice.dto.response.ProductSummaryResponse;
import com.ecomerce.productservice.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ProductService {
    
    // ==================== PUBLIC ENDPOINTS (Summary) ====================
    
    /**
     * Lấy danh sách products với phân trang (chỉ ảnh + tên + giá)
     * Hỗ trợ filtering qua Specification từ @Filter annotation
     */
    PageResponseDto<ProductSummaryResponse> getAllProducts(Specification<Product> spec, Pageable pageable);
    
    /**
     * Tìm kiếm products theo tên (chỉ ảnh + tên + giá)
     */
    PageResponseDto<ProductSummaryResponse> searchProducts(String keyword, Pageable pageable);
    
    /**
     * Lấy products theo category (chỉ ảnh + tên + giá)
     */
    PageResponseDto<ProductSummaryResponse> getProductsByCategory(Long categoryId, Pageable pageable);
    
    /**
     * Lấy featured products (chỉ ảnh + tên + giá)
     */
    PageResponseDto<ProductSummaryResponse> getFeaturedProducts(Pageable pageable);
    
    // ==================== PUBLIC ENDPOINTS (Detail) ====================
    
    /**
     * Lấy product theo ID (chi tiết đầy đủ)
     */
    ProductDetailResponse getProductById(Long id);
    
    /**
     * Lấy product theo slug (chi tiết đầy đủ)
     */
    ProductDetailResponse getProductBySlug(String slug);
    
    // ==================== ADMIN ENDPOINTS (Admin Response) ====================
    

    /**
     * Lấy danh sách products cho admin với Specification filter (có thêm ngày giờ)
     */
    PageResponseDto<ProductAdminResponse> getAllProductsForAdmin(
            org.springframework.data.jpa.domain.Specification<com.ecomerce.productservice.model.Product> spec,
            Pageable pageable
    );
    
    /**
     * Lấy danh sách products cho admin (tối giản - list view)
     * Chỉ trả về thông tin cần thiết cho list view
     */
    PageResponseDto<ProductListResponse> getAllProductsForAdminList(
            Specification<Product> spec,
            Pageable pageable
    );
    
    /**
     * Lấy product theo ID cho admin (có thêm ngày giờ)
     */
    ProductAdminResponse getProductByIdForAdmin(Long id);
    
    /**
     * Tạo product mới (trả về admin response)
     * Tự động upload ảnh nếu có trong request
     */
    ProductAdminResponse createProduct(ProductCreateRequest request);
    
    /**
     * Cập nhật product (trả về admin response)
     * Tự động upload ảnh mới và xóa ảnh cũ nếu có
     */
    ProductAdminResponse updateProduct(Long id, ProductUpdateRequest request);
    
    /**
     * Partial update product - chỉ update fields được gửi lên
     * @param id Product ID
     * @param updates Map chứa các fields cần update
     * @return ProductAdminResponse sau khi update
     */
    ProductAdminResponse patchProduct(Long id, Map<String, Object> updates);
    
    /**
     * Xóa product (soft delete - set active = false)
     */
    void deleteProduct(Long id);
    
    // ==================== IMAGE UPLOAD ENDPOINTS ====================
    
    /**
     * Upload ảnh đơn lẻ lên S3 (dùng cho thumbnail)
     * @param imageFile File ảnh cần upload
     * @param productId ID của product (null nếu chưa có product)
     * @return URL của ảnh đã upload lên S3
     */
    String uploadImage(MultipartFile imageFile, Long productId);
    
    /**
     * Upload nhiều ảnh cùng lúc lên S3 (dùng cho images list)
     * @param imageFiles Danh sách file ảnh cần upload
     * @param productId ID của product (null nếu chưa có product)
     * @return Danh sách URL của các ảnh đã upload lên S3
     */
    java.util.List<String> uploadImages(MultipartFile[] imageFiles, Long productId);
}

