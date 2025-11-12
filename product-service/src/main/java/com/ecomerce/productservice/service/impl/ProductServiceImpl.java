package com.ecomerce.productservice.service.impl;

import com.ecomerce.productservice.advice.exeption.ResourceAlreadyExistsException;
import com.ecomerce.productservice.advice.exeption.ResourceNotFoundException;
import com.ecomerce.productservice.dto.CategoryInfo;
import com.ecomerce.productservice.dto.request.ProductCreateRequest;
import com.ecomerce.productservice.dto.request.ProductUpdateRequest;
import com.ecomerce.productservice.dto.response.PageResponseDto;
import com.ecomerce.productservice.dto.response.ProductAdminResponse;
import com.ecomerce.productservice.dto.response.ProductDetailResponse;
import com.ecomerce.productservice.dto.response.ProductListResponse;
import com.ecomerce.productservice.dto.response.ProductSummaryResponse;
import com.ecomerce.productservice.model.Category;
import com.ecomerce.productservice.model.Product;
import com.ecomerce.productservice.repository.CategoryRepository;
import com.ecomerce.productservice.repository.ProductRepository;
import com.ecomerce.productservice.service.ProductService;
import com.ecomerce.productservice.service.S3Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPLICATE_DASHES = Pattern.compile("-+");

    // ==================== PUBLIC ENDPOINTS (Summary) ====================

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductSummaryResponse> getAllProducts(Specification<Product> spec, Pageable pageable) {
        // Logic filtering nằm ở Repository layer (findActiveWithFilter)
        // Tự động combine với active=true để chỉ lấy products đang hoạt động
        Page<Product> products = productRepository.findActiveWithFilter(spec, pageable);
        return convertToSummaryPageResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductSummaryResponse> searchProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.searchByName(keyword, pageable);
        return convertToSummaryPageResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductSummaryResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        // Kiểm tra category tồn tại
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));
        
        Page<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return convertToSummaryPageResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductSummaryResponse> getFeaturedProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByFeaturedTrueAndActiveTrue(pageable);
        return convertToSummaryPageResponse(products);
    }

    // ==================== PUBLIC ENDPOINTS (Detail) ====================

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Sản phẩm không còn hoạt động");
        }
        
        return convertToDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugWithCategory(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với slug: " + slug));
        
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Sản phẩm không còn hoạt động");
        }
        
        return convertToDetailResponse(product);
    }

    // ==================== ADMIN ENDPOINTS (Admin Response) ====================


    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductAdminResponse> getAllProductsForAdmin(
            org.springframework.data.jpa.domain.Specification<Product> spec,
            Pageable pageable
    ) {
        // Sử dụng repository với Specification
        Page<Product> products = productRepository.findAllWithFilter(spec, pageable);
        return convertToAdminPageResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductListResponse> getAllProductsForAdminList(
            Specification<Product> spec,
            Pageable pageable
    ) {
        // Sử dụng repository với Specification
        Page<Product> products = productRepository.findAllWithFilter(spec, pageable);
        return convertToListPageResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAdminResponse getProductByIdForAdmin(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        
        return convertToAdminResponse(product);
    }

    @Override
    @Transactional
    public ProductAdminResponse createProduct(ProductCreateRequest request) {
        String userEmail = getCurrentUserEmail();
        if (userEmail != null) {
            log.info("📝 User {} đang tạo sản phẩm mới", userEmail);
        }
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
        
        String slug = generateSlug(request.getName());
        if (productRepository.existsBySlug(slug)) {
            throw new ResourceAlreadyExistsException("Slug '" + slug + "' đã tồn tại");
        }
        
        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .discountStartDate(request.getDiscountStartDate())
                .discountEndDate(request.getDiscountEndDate())
                .thumbnail(request.getThumbnailUrl())
                .images(request.getImageUrls())
                .category(category)
                .active(request.isActive())
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("✅ Đã tạo sản phẩm: {} - {}", savedProduct.getId(), savedProduct.getName());
        
        return convertToAdminResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductAdminResponse updateProduct(Long id, ProductUpdateRequest request) {
        String userEmail = getCurrentUserEmail();
        if (userEmail != null) {
            log.info("✏️ User {} đang cập nhật sản phẩm ID: {}", userEmail, id);
        }
        
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        
        // Update fields (chỉ update nếu không null)
        updateProductFields(product, request);
        
        Product updatedProduct = productRepository.save(product);
        log.info("✅ Đã cập nhật sản phẩm: {} - {}", updatedProduct.getId(), updatedProduct.getName());
        
        return convertToAdminResponse(updatedProduct);
    }
    
    /**
     * Helper method: Update product fields từ request (chỉ update nếu không null)
     */
    private void updateProductFields(Product product, ProductUpdateRequest request) {
        if (request.getName() != null) {
            String newSlug = generateSlug(request.getName());
            if (!product.getSlug().equals(newSlug) && productRepository.existsBySlugAndIdNot(newSlug, product.getId())) {
                throw new ResourceAlreadyExistsException("Slug '" + newSlug + "' đã tồn tại");
            }
            product.setName(request.getName());
            product.setSlug(newSlug);
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDiscountPrice() != null) {
            product.setDiscountPrice(request.getDiscountPrice());
        }
        if (request.getDiscountStartDate() != null) {
            product.setDiscountStartDate(request.getDiscountStartDate());
        }
        if (request.getDiscountEndDate() != null) {
            product.setDiscountEndDate(request.getDiscountEndDate());
        }
        if (request.getThumbnailUrl() != null) {
            product.setThumbnail(request.getThumbnailUrl());
        }
        if (request.getImageUrls() != null) {
            product.setImages(request.getImageUrls());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }
    }

    @Override
    @Transactional
    public ProductAdminResponse patchProduct(Long id, java.util.Map<String, Object> updates) {
        String userEmail = getCurrentUserEmail();
        if (userEmail != null) {
            log.info("🔧 User {} đang partial update sản phẩm ID: {}", userEmail, id);
        }
        
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        
        // Chỉ update fields được gửi lên
        if (updates.containsKey("name")) {
            String name = (String) updates.get("name");
            String newSlug = generateSlug(name);
            if (!product.getSlug().equals(newSlug) && productRepository.existsBySlugAndIdNot(newSlug, id)) {
                throw new ResourceAlreadyExistsException("Slug '" + newSlug + "' đã tồn tại");
            }
            product.setName(name);
            product.setSlug(newSlug);
        }
        if (updates.containsKey("description")) {
            product.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("price")) {
            product.setPrice(new java.math.BigDecimal(updates.get("price").toString()));
        }
        if (updates.containsKey("discountPrice")) {
            Object discountPrice = updates.get("discountPrice");
            product.setDiscountPrice(discountPrice != null ? new java.math.BigDecimal(discountPrice.toString()) : null);
        }
        if (updates.containsKey("discountStartDate")) {
            product.setDiscountStartDate(java.time.LocalDateTime.parse(updates.get("discountStartDate").toString()));
        }
        if (updates.containsKey("discountEndDate")) {
            product.setDiscountEndDate(java.time.LocalDateTime.parse(updates.get("discountEndDate").toString()));
        }
        if (updates.containsKey("categoryId")) {
            Long categoryId = Long.valueOf(updates.get("categoryId").toString());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));
            product.setCategory(category);
        }
        if (updates.containsKey("active")) {
            product.setActive((Boolean) updates.get("active"));
        }
        if (updates.containsKey("featured")) {
            product.setFeatured((Boolean) updates.get("featured"));
        }
        if (updates.containsKey("thumbnailUrl")) {
            product.setThumbnail((String) updates.get("thumbnailUrl"));
        }
        if (updates.containsKey("imageUrls")) {
            @SuppressWarnings("unchecked")
            java.util.List<String> imageUrls = (java.util.List<String>) updates.get("imageUrls");
            product.setImages(imageUrls);
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("✅ Đã partial update sản phẩm: {} - {}", updatedProduct.getId(), updatedProduct.getName());
        
        return convertToAdminResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        
        // Soft delete - set active = false
        product.setActive(false);
        productRepository.save(product);
        log.info("Đã xóa sản phẩm (soft delete): {}", id);
    }

    // ==================== CONVERTER METHODS ====================

    /**
     * Chuyển đổi Product thành ProductSummaryResponse (chỉ ảnh + tên + giá)
     * Cho trang web - tối ưu performance
     */
    private ProductSummaryResponse convertToSummaryResponse(Product product) {
        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .thumbnail(product.getThumbnail())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .build();
    }

    /**
     * Chuyển đổi Product thành ProductDetailResponse (đầy đủ thông tin)
     * Cho trang chi tiết sản phẩm
     */
    private ProductDetailResponse convertToDetailResponse(Product product) {
        CategoryInfo categoryInfo = null;
        if (product.getCategory() != null) {
            categoryInfo = CategoryInfo.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .slug(product.getCategory().getSlug())
                    .build();
        }

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .discountStartDate(product.getDiscountStartDate())
                .discountEndDate(product.getDiscountEndDate())
                .thumbnail(product.getThumbnail())
                .images(product.getImages())
                .category(categoryInfo)
                .active(product.isActive())
                .featured(product.getFeatured())
                .build();
    }

    /**
     * Chuyển đổi Product thành ProductAdminResponse (đầy đủ + ngày giờ)
     * Cho giao diện admin
     */
    private ProductAdminResponse convertToAdminResponse(Product product) {
        CategoryInfo categoryInfo = null;
        if (product.getCategory() != null) {
            categoryInfo = CategoryInfo.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .slug(product.getCategory().getSlug())
                    .build();
        }

        return ProductAdminResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .discountStartDate(product.getDiscountStartDate())
                .discountEndDate(product.getDiscountEndDate())
                .thumbnail(product.getThumbnail())
                .images(product.getImages())
                .category(categoryInfo)
                .active(product.isActive())
                .featured(product.getFeatured())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Lấy email từ JWT token hiện tại (không cần gọi Feign Client)
     */
    private String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getSubject(); // JWT subject là email
            }
        } catch (Exception e) {
            log.debug("Không thể lấy email từ JWT token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Chuyển đổi Page<Product> thành PageResponseDto<ProductSummaryResponse>
     */
    private PageResponseDto<ProductSummaryResponse> convertToSummaryPageResponse(Page<Product> productPage) {
        PageResponseDto<ProductSummaryResponse> response = new PageResponseDto<>();
        response.setContent(productPage.getContent().stream()
                .map(this::convertToSummaryResponse)
                .toList());
        response.setPage(productPage.getNumber() + 1); // 1-indexed
        response.setSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        return response;
    }

    /**
     * Chuyển đổi Page<Product> thành PageResponseDto<ProductAdminResponse>
     * Sử dụng Feign Client để enrich thông tin user
     */
    private PageResponseDto<ProductAdminResponse> convertToAdminPageResponse(Page<Product> productPage) {
        PageResponseDto<ProductAdminResponse> response = new PageResponseDto<>();
        
        // Convert từng product và enrich với user info từ auth-service
        response.setContent(productPage.getContent().stream()
                .map(this::convertToAdminResponse)
                .toList());
        
        response.setPage(productPage.getNumber() + 1); // 1-indexed
        response.setSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        return response;
    }

    /**
     * Chuyển đổi Page<Product> thành PageResponseDto<ProductListResponse>
     * Tối giản - chỉ thông tin cần thiết cho list view
     */
    private PageResponseDto<ProductListResponse> convertToListPageResponse(Page<Product> productPage) {
        PageResponseDto<ProductListResponse> response = new PageResponseDto<>();
        
        response.setContent(productPage.getContent().stream()
                .map(this::convertToListResponse)
                .toList());
        
        response.setPage(productPage.getNumber() + 1); // 1-indexed
        response.setSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        return response;
    }

    /**
     * Chuyển đổi Product thành ProductListResponse (tối giản)
     * Chỉ chứa thông tin cần thiết cho list view
     */
    private ProductListResponse convertToListResponse(Product product) {
        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .thumbnail(product.getThumbnail() != null ? product.getThumbnail() : "")
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .active(product.isActive())
                .featured(product.getFeatured())
                .build();
    }

    /**
     * Tạo slug từ tên sản phẩm (Vietnamese friendly)
     * Ví dụ: "Thiết Bị Điện Tử" -> "thiet-bi-dien-tu"
     */
    private String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
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
    
    // ==================== IMAGE UPLOAD METHODS ====================
    
    @Override
    @Transactional
    public String uploadImage(MultipartFile imageFile, Long productId) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }
        
        // Tạo tên file: product-{productId}-{timestamp}-{originalFilename}
        // Nếu productId null (chưa có product), dùng "temp" thay thế
        String folder = "products";
        String fileName;
        if (productId != null) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalFilename = imageFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            fileName = String.format("product-%d-%s%s", productId, timestamp, extension);
        } else {
            // Trường hợp upload ảnh trước khi tạo product
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalFilename = imageFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            fileName = String.format("temp-%s%s", timestamp, extension);
        }
        
        // Upload lên S3 và lấy URL
        String url = s3Service.uploadFile(imageFile, folder, fileName, true);
        log.info("✅ Đã upload ảnh lên S3: {}", url);
        
        return url;
    }
    
    @Override
    @Transactional
    public java.util.List<String> uploadImages(MultipartFile[] imageFiles, Long productId) {
        if (imageFiles == null || imageFiles.length == 0) {
            throw new IllegalArgumentException("Danh sách file ảnh không được để trống");
        }
        
        java.util.List<String> urls = new java.util.ArrayList<>();
        
        for (int i = 0; i < imageFiles.length; i++) {
            MultipartFile imageFile = imageFiles[i];
            if (imageFile == null || imageFile.isEmpty()) {
                log.warn("⚠️ File ảnh thứ {} bị bỏ qua vì null hoặc rỗng", i + 1);
                continue;
            }
            
            // Tạo tên file: product-{productId}-{index}-{timestamp}-{originalFilename}
            String folder = "products";
            String fileName;
            if (productId != null) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String originalFilename = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                fileName = String.format("product-%d-%d-%s%s", productId, i, timestamp, extension);
            } else {
                // Trường hợp upload ảnh trước khi tạo product
                String timestamp = String.valueOf(System.currentTimeMillis());
                String originalFilename = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                fileName = String.format("temp-%d-%s%s", i, timestamp, extension);
            }
            
            // Upload lên S3 và lấy URL
            String url = s3Service.uploadFile(imageFile, folder, fileName, true);
            urls.add(url);
            log.info("✅ Đã upload ảnh {} lên S3: {}", i + 1, url);
        }
        
        log.info("✅ Đã upload {} ảnh lên S3", urls.size());
        return urls;
    }
}
