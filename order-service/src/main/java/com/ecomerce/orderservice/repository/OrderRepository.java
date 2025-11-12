package com.ecomerce.orderservice.repository;

import com.ecomerce.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    
    /**
     * Lấy orders của user với JOIN FETCH items để tránh N+1
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * Lấy order theo orderNumber với JOIN FETCH items để tránh N+1
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);
    
    /**
     * Lấy order theo ID với JOIN FETCH items để tránh N+1
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    /**
     * Lấy order theo orderNumber (không có JOIN FETCH - dùng khi không cần items)
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Lấy orders theo status với JOIN FETCH items
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.status = :status")
    List<Order> findByStatusWithItems(@Param("status") Order.OrderStatus status);
    
    /**
     * Lấy orders theo status (không có JOIN FETCH - dùng khi không cần items)
     */
    List<Order> findByStatus(Order.OrderStatus status);
}

