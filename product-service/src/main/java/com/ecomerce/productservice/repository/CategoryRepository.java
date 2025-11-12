package com.ecomerce.productservice.repository;

import com.ecomerce.productservice.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends
        JpaRepository<Category, Long>,
        JpaSpecificationExecutor<Category> {

    /**
     * Tìm category theo slug
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại chưa (loại trừ category hiện tại khi update)
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    boolean existsBySlug(String slug);

    /**
     * Tìm categories với filter specification và pagination
     * Hỗ trợ filtering linh hoạt với Specification
     */
    default Page<Category> findAllWithFilter(
            Specification<Category> filterSpec,
            Pageable pageable
    ) {
        return findAll(filterSpec, pageable);
    }



}

