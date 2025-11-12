package com.ecomerce.productservice.repository;

import com.ecomerce.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends
        JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /**
     * Tìm product theo ID với JOIN FETCH category để tránh N+1
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    /**
     * Tìm product theo slug với JOIN FETCH category để tránh N+1
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.slug = :slug")
    Optional<Product> findBySlugWithCategory(@Param("slug") String slug);

    /**
     * Tìm tất cả products đang active (không có JOIN FETCH - dùng cho pagination)
     */
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    
    /**
    /**
     * Tìm products featured (không có JOIN FETCH - dùng cho pagination)
     */
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);

    /**
     * Tìm kiếm products theo tên (case-insensitive) với JOIN FETCH category
     */
    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND p.active = true",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE " +
                   "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "AND p.active = true")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Kiểm tra slug đã tồn tại chưa (loại trừ product hiện tại khi update)
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    boolean existsBySlug(String slug);

    /**
     * Tìm products với filter specification và pagination
     * Hỗ trợ filtering linh hoạt với Specification (không áp dụng active filter)
     */
    default Page<Product> findAllWithFilter(
            Specification<Product> filterSpec,
            Pageable pageable
    ) {
        return findAll(filterSpec, pageable);
    }

    /**
     * Tìm products active với filter specification từ @Filter
     * Tự động combine với active=true để chỉ lấy products đang hoạt động
     * Dùng cho public endpoints (chỉ hiển thị products active)
     */
    default Page<Product> findActiveWithFilter(
            Specification<Product> filterSpec,
            Pageable pageable
    ) {
        Specification<Product> activeSpec = (root, query, cb) ->
                cb.equal(root.get("active"), true);
        
        Specification<Product> combined = activeSpec;
        if (filterSpec != null) {
            combined = activeSpec.and(filterSpec);
        }
        
        return findAll(combined, pageable);
    }

}

