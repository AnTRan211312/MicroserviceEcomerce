package com.ecomerce.productservice.model;

import com.ecomerce.productservice.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_slug", columnList = "slug"), // Unique lookup
    @Index(name = "idx_product_category_id", columnList = "category_id"), // Foreign key
    @Index(name = "idx_product_active", columnList = "active"), // Filter by active status
    @Index(name = "idx_product_featured", columnList = "featured"), // Featured products
    @Index(name = "idx_product_price", columnList = "price"), // Price range queries
    @Index(name = "idx_product_created_at", columnList = "created_at"), // For sorting
    // Composite indexes for common queries
    @Index(name = "idx_product_active_featured", columnList = "active, featured"),
    @Index(name = "idx_product_category_active", columnList = "category_id, active")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(precision = 15, scale = 2)
    private BigDecimal discountPrice;

    @Column
    private LocalDateTime discountStartDate;

    @Column
    private LocalDateTime discountEndDate;

    @Column(length = 1000)
    private String thumbnail;
    @ElementCollection()
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column
    @Builder.Default
    private Boolean featured = false;

}
