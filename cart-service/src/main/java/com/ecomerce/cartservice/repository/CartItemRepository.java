package com.ecomerce.cartservice.repository;

import com.ecomerce.cartservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByCartId(Long cartId);
    
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    void deleteByCartId(Long cartId);
    
    /**
     * Tìm cart item theo ID và userId của cart
     * Đảm bảo cart item thuộc về user hiện tại
     */
    @Query("SELECT ci FROM CartItem ci JOIN ci.cart c WHERE ci.id = :itemId AND c.userId = :userId AND c.isActive = true")
    Optional<CartItem> findByIdAndCartUserId(@Param("itemId") Long itemId, @Param("userId") Long userId);
}

