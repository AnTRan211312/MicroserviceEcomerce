package com.ecomerce.inventoryservice.service.impl;

import com.ecomerce.inventoryservice.advice.exeption.ResourceAlreadyExistsException;
import com.ecomerce.inventoryservice.advice.exeption.ResourceNotFoundException;
import com.ecomerce.inventoryservice.client.ProductServiceClient;
import com.ecomerce.inventoryservice.client.dto.ProductInfoResponse;
import com.ecomerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecomerce.inventoryservice.dto.request.InventoryCreateRequest;
import com.ecomerce.inventoryservice.dto.request.InventoryUpdateRequest;
import com.ecomerce.inventoryservice.dto.response.InventoryResponse;
import com.ecomerce.inventoryservice.dto.response.PageResponseDto;
import com.ecomerce.inventoryservice.model.Inventory;
import com.ecomerce.inventoryservice.repository.InventoryRepository;
import com.ecomerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public InventoryResponse createInventory(InventoryCreateRequest request) {
        // Check if inventory already exists for this product
        if (inventoryRepository.findByProductId(request.getProductId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Kho hàng cho sản phẩm này đã tồn tại");
        }

        // Validate product exists via Feign Client
        try {
            com.ecomerce.inventoryservice.config.feign.ProductServiceResponse<ProductInfoResponse> response = 
                    productServiceClient.getProductById(request.getProductId());
            
            if (response == null || response.getData() == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại với ID: " + request.getProductId());
            }
            
            ProductInfoResponse productInfo = response.getData();
            if (!productInfo.getActive()) {
                throw new IllegalArgumentException("Sản phẩm đã bị vô hiệu hóa");
            }
            log.info("✅ Validated product exists: {} - {}", productInfo.getId(), productInfo.getName());
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại với ID: " + request.getProductId());
            }
            throw e;
        } catch (Exception e) {
            log.error("❌ Error validating product via Feign Client: {}", e.getMessage());
            throw new RuntimeException("Không thể xác thực sản phẩm. Vui lòng thử lại sau.");
        }

        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .reservedQuantity(0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .isActive(true)
                .build();

        inventory.calculateAvailableQuantity();
        inventory = inventoryRepository.save(inventory);
        log.info("✅ Created inventory for product: {} with quantity: {}", 
                request.getProductId(), inventory.getQuantity());

        return mapToResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng cho sản phẩm: " + productId));

        return mapToResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng với ID: " + id));

        return mapToResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        List<Inventory> inventories = inventoryRepository.findByIsActiveTrue();
        return inventories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventoryResponse> getAllInventories(Specification<Inventory> spec, Pageable pageable) {
        Page<Inventory> inventoryPage = inventoryRepository.findAll(spec, pageable);
        
        List<InventoryResponse> content = inventoryPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                inventoryPage.getNumber(),
                inventoryPage.getSize(),
                inventoryPage.getTotalElements(),
                inventoryPage.getTotalPages()
        );
    }

    @Override
    public InventoryResponse updateInventory(Long id, InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng với ID: " + id));

        if (request.getQuantity() != null) {
            inventory.updateQuantity(request.getQuantity());
        }

        if (request.getLowStockThreshold() != null) {
            inventory.setLowStockThreshold(request.getLowStockThreshold());
        }

        if (request.getIsActive() != null) {
            inventory.setIsActive(request.getIsActive());
        }

        inventory = inventoryRepository.save(inventory);
        log.info("✅ Updated inventory: {} for product: {}", id, inventory.getProductId());

        return mapToResponse(inventory);
    }

    @Override
    public InventoryResponse adjustInventory(Long id, InventoryAdjustRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng với ID: " + id));

        if (request.getQuantity() > 0) {
            inventory.addQuantity(request.getQuantity());
            log.info("✅ Added quantity: {} to inventory: {} (Reason: {})", 
                    request.getQuantity(), id, request.getReason());
        } else if (request.getQuantity() < 0) {
            inventory.deductQuantity(Math.abs(request.getQuantity()));
            log.info("✅ Deducted quantity: {} from inventory: {} (Reason: {})", 
                    Math.abs(request.getQuantity()), id, request.getReason());
        }

        inventory = inventoryRepository.save(inventory);
        return mapToResponse(inventory);
    }

    @Override
    public void reserveQuantity(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng cho sản phẩm: " + productId));

        inventory.reserveQuantity(quantity);
        inventoryRepository.save(inventory);
        log.info("✅ Reserved quantity: {} for product: {}", quantity, productId);
    }

    @Override
    public void releaseReservedQuantity(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng cho sản phẩm: " + productId));

        inventory.releaseReservedQuantity(quantity);
        inventoryRepository.save(inventory);
        log.info("✅ Released reserved quantity: {} for product: {}", quantity, productId);
    }

    @Override
    public void deductQuantity(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng cho sản phẩm: " + productId));

        inventory.deductQuantity(quantity);
        inventoryRepository.save(inventory);
        log.info("✅ Deducted quantity: {} for product: {}", quantity, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems() {
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();
        return lowStockItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkAvailability(Long productId, Integer quantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.getAvailableQuantity() >= quantity)
                .orElse(false);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .isActive(inventory.getIsActive())
                .isLowStock(inventory.isLowStock())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}

