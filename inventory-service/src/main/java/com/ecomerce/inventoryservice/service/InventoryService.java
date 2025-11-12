package com.ecomerce.inventoryservice.service;

import com.ecomerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecomerce.inventoryservice.dto.request.InventoryCreateRequest;
import com.ecomerce.inventoryservice.dto.request.InventoryUpdateRequest;
import com.ecomerce.inventoryservice.dto.response.InventoryResponse;
import com.ecomerce.inventoryservice.dto.response.PageResponseDto;
import com.ecomerce.inventoryservice.model.Inventory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface InventoryService {
    InventoryResponse createInventory(InventoryCreateRequest request);
    
    InventoryResponse getInventoryByProductId(Long productId);
    
    InventoryResponse getInventoryById(Long id);
    
    List<InventoryResponse> getAllInventories();
    
    PageResponseDto<InventoryResponse> getAllInventories(Specification<Inventory> spec, Pageable pageable);
    
    InventoryResponse updateInventory(Long id, InventoryUpdateRequest request);
    
    InventoryResponse adjustInventory(Long id, InventoryAdjustRequest request);
    
    void reserveQuantity(Long productId, Integer quantity);
    
    void releaseReservedQuantity(Long productId, Integer quantity);
    
    void deductQuantity(Long productId, Integer quantity);
    
    List<InventoryResponse> getLowStockItems();
    
    Boolean checkAvailability(Long productId, Integer quantity);
}

