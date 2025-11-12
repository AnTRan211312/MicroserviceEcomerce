package com.ecomerce.inventoryservice.controller;

import com.ecomerce.inventoryservice.annontation.ApiMessage;
import com.ecomerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecomerce.inventoryservice.dto.request.InventoryCreateRequest;
import com.ecomerce.inventoryservice.dto.request.InventoryUpdateRequest;
import com.ecomerce.inventoryservice.dto.response.InventoryResponse;
import com.ecomerce.inventoryservice.dto.response.PageResponseDto;
import com.ecomerce.inventoryservice.model.Inventory;
import com.ecomerce.inventoryservice.service.InventoryService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "Quản lý kho hàng")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @ApiMessage(value = "Tạo kho hàng thành công")
    @PreAuthorize("hasAuthority('POST /api/inventory')")
    @Operation(
            summary = "Tạo kho hàng mới",
            description = "Yêu cầu quyền: <b>POST /api/inventory</b>"
    )
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody InventoryCreateRequest request) {
        InventoryResponse inventory = inventoryService.createInventory(request);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}")
    @ApiMessage(value = "Lấy thông tin kho hàng thành công")
    @Operation(summary = "Lấy thông tin kho hàng theo Product ID")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(
            @PathVariable Long productId) {
        InventoryResponse inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{id}")
    @ApiMessage(value = "Lấy thông tin kho hàng thành công")
    @Operation(summary = "Lấy thông tin kho hàng theo ID")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        InventoryResponse inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Lấy danh sách tất cả kho hàng với filtering (PROTECTED - cần permission GET /api/inventory)
     * Hỗ trợ filtering qua query string: ?productId=1&isActive=true&quantity>100&availableQuantity<50
     * Hỗ trợ pagination: ?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    @ApiMessage(value = "Lấy danh sách kho hàng thành công")
    @PreAuthorize("hasAuthority('GET /api/inventory')")
    @Operation(
            summary = "Lấy danh sách tất cả kho hàng với filtering",
            description = "Yêu cầu quyền: <b>GET /api/inventory</b>. Hỗ trợ filtering qua query string. Ví dụ: ?productId=1&isActive=true&quantity>100&availableQuantity<50&page=0&size=20&sort=createdAt,desc"
    )
    public ResponseEntity<PageResponseDto<InventoryResponse>> getAllInventories(
            @Filter Specification<Inventory> spec,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDto<InventoryResponse> inventories = inventoryService.getAllInventories(spec, pageable);
        return ResponseEntity.ok(inventories);
    }

    @PutMapping("/{id}")
    @ApiMessage(value = "Cập nhật kho hàng thành công")
    @PreAuthorize("hasAuthority('PUT /api/inventory/{id}')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cập nhật kho hàng",
            description = "Yêu cầu quyền: <b>PUT /api/inventory/{id}</b>"
    )
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryUpdateRequest request) {
        InventoryResponse inventory = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{id}/adjust")
    @ApiMessage(value = "Điều chỉnh số lượng thành công")
    @PreAuthorize("hasAuthority('POST /api/inventory/{id}/adjust')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Điều chỉnh số lượng kho hàng",
            description = "Yêu cầu quyền: <b>POST /api/inventory/{id}/adjust</b>"
    )
    public ResponseEntity<InventoryResponse> adjustInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryAdjustRequest request) {
        InventoryResponse inventory = inventoryService.adjustInventory(id, request);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/low-stock")
    @ApiMessage(value = "Lấy danh sách sản phẩm sắp hết hàng thành công")
    @PreAuthorize("hasAuthority('GET /api/inventory/low-stock')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lấy danh sách sản phẩm sắp hết hàng",
            description = "Yêu cầu quyền: <b>GET /api/inventory/low-stock</b>"
    )
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        List<InventoryResponse> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/product/{productId}/check")
    @ApiMessage(value = "Kiểm tra số lượng hàng thành công")
    @Operation(summary = "Kiểm tra số lượng hàng có sẵn")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        Boolean available = inventoryService.checkAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }
}
