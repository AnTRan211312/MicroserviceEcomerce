package com.ecomerce.cartservice.repository;

import com.ecomerce.cartservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {
    
    /**
     * Lấy cart của user với JOIN FETCH items để tránh N+1
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId AND c.isActive = true")
    Optional<Cart> findByUserIdAndIsActiveTrueWithItems(@Param("userId") Long userId);
    
    /**
     * Lấy cart của user (không có JOIN FETCH - dùng khi không cần items)
     */
    Optional<Cart> findByUserIdAndIsActiveTrue(Long userId);
    
    /**
     * Lấy cart của user với JOIN FETCH items
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
    
    /**
     * Lấy cart của user (không có JOIN FETCH - dùng khi không cần items)
     */
    Optional<Cart> findByUserId(Long userId);
    
    /**
     * Lấy cart theo ID với JOIN FETCH items
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") Long id);
    
    boolean existsByUserIdAndIsActiveTrue(Long userId);
}

