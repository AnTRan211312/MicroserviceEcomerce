package com.ecomerce.inventoryservice.model;

import com.ecomerce.inventoryservice.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventories", indexes = {
    @Index(name = "idx_inventory_product_id", columnList = "product_id"), // Unique lookup
    @Index(name = "idx_inventory_is_active", columnList = "is_active"), // Filter active inventories
    @Index(name = "idx_inventory_available_quantity", columnList = "available_quantity"), // Low stock queries
    @Index(name = "idx_inventory_created_at", columnList = "created_at"), // For sorting
    // Composite index for low stock detection
    @Index(name = "idx_inventory_active_available", columnList = "is_active, available_quantity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", unique = true, nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Helper methods
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        this.availableQuantity = this.quantity - this.reservedQuantity;
    }
    
    public void calculateAvailableQuantity() {
        this.availableQuantity = this.quantity - this.reservedQuantity;
    }

    public void reserveQuantity(Integer amount) {
        if (availableQuantity < amount) {
            throw new IllegalStateException("Không đủ hàng trong kho. Còn lại: " + availableQuantity);
        }
        this.reservedQuantity += amount;
        calculateAvailableQuantity();
    }

    public void releaseReservedQuantity(Integer amount) {
        if (reservedQuantity < amount) {
            throw new IllegalStateException("Số lượng đã đặt trước không hợp lệ");
        }
        this.reservedQuantity -= amount;
        calculateAvailableQuantity();
    }

    public void deductQuantity(Integer amount) {
        // Validate reserved quantity first (when order is delivered, quantity was already reserved)
        if (reservedQuantity < amount) {
            throw new IllegalStateException(
                String.format("Không đủ số lượng đã reserve để deduct. Reserved: %d, Requested: %d", 
                    reservedQuantity, amount)
            );
        }
        // Validate quantity (safety check)
        if (quantity < amount) {
            throw new IllegalStateException(
                String.format("Không đủ hàng trong kho. Quantity: %d, Requested: %d", 
                    quantity, amount)
            );
        }
        // Deduct both reservedQuantity and quantity
        this.reservedQuantity -= amount;
        this.quantity -= amount;
        calculateAvailableQuantity();
    }

    public void addQuantity(Integer amount) {
        this.quantity += amount;
        this.availableQuantity = this.quantity - this.reservedQuantity;
    }

    public boolean isLowStock() {
        return availableQuantity <= lowStockThreshold;
    }
}

