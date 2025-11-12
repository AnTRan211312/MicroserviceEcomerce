package com.ecomerce.inventoryservice.repository;

import com.ecomerce.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {
    
    Optional<Inventory> findByProductId(Long productId);
    
    List<Inventory> findByIsActiveTrue();
    
    List<Inventory> findByIsActiveTrueAndAvailableQuantityLessThanEqual(Integer threshold);
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.lowStockThreshold AND i.isActive = true")
    List<Inventory> findLowStockItems();
}

