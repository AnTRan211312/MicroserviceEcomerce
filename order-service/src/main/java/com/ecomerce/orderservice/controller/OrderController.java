package com.ecomerce.orderservice.controller;

import com.ecomerce.orderservice.annontation.ApiMessage;
import com.ecomerce.orderservice.dto.request.BuyNowRequest;
import com.ecomerce.orderservice.dto.request.OrderCreateRequest;
import com.ecomerce.orderservice.dto.request.OrderUpdateRequest;
import com.ecomerce.orderservice.dto.response.OrderResponse;
import com.ecomerce.orderservice.dto.response.PageResponseDto;
import com.ecomerce.orderservice.model.Order;
import com.ecomerce.orderservice.service.OrderService;
import com.ecomerce.orderservice.util.JwtUtil;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Order", description = "Quản lý đơn hàng")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ApiMessage(value = "Tạo đơn hàng thành công")
    @Operation(summary = "Tạo đơn hàng mới (yêu cầu gửi đầy đủ thông tin sản phẩm)")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = JwtUtil.getCurrentUserId();
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/buy-now")
    @ApiMessage(value = "Mua ngay thành công")
    @Operation(summary = "Mua ngay sản phẩm (chỉ cần productId và quantity, hệ thống tự lấy giá và kiểm tra tồn kho)")
    public ResponseEntity<OrderResponse> buyNow(@Valid @RequestBody BuyNowRequest request) {
        Long userId = JwtUtil.getCurrentUserId();
        OrderResponse order = orderService.buyNow(userId, request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    @ApiMessage(value = "Lấy thông tin đơn hàng thành công")
    @Operation(summary = "Lấy thông tin đơn hàng theo ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        Long userId = JwtUtil.getCurrentUserId();
        OrderResponse order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    @ApiMessage(value = "Lấy thông tin đơn hàng thành công")
    @Operation(summary = "Lấy thông tin đơn hàng theo mã đơn hàng")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Long userId = JwtUtil.getCurrentUserId();
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber, userId);
        return ResponseEntity.ok(order);
    }

    /**
     * Lấy danh sách đơn hàng của user với filtering và pagination
     * Hỗ trợ filtering qua query string: ?status=PENDING&totalAmount>1000&createdAt>2024-01-01
     * Hỗ trợ pagination: ?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/my-orders")
    @ApiMessage(value = "Lấy danh sách đơn hàng thành công")
    @Operation(
            summary = "Lấy danh sách đơn hàng của user với filtering và pagination",
            description = "Hỗ trợ filtering qua query string. Ví dụ: ?status=PENDING&totalAmount>1000&createdAt>2024-01-01&page=0&size=20&sort=createdAt,desc"
    )
    public ResponseEntity<PageResponseDto<OrderResponse>> getMyOrders(
            @Filter Specification<Order> spec,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = JwtUtil.getCurrentUserId();
        PageResponseDto<OrderResponse> orders = orderService.getUserOrders(userId, spec, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Lấy danh sách tất cả đơn hàng cho admin với filtering (PROTECTED - cần permission GET /api/orders)
     * Hỗ trợ filtering qua query string: ?userId=1&status=PENDING&totalAmount>1000&createdAt>2024-01-01
     * Hỗ trợ pagination: ?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    @ApiMessage(value = "Lấy danh sách đơn hàng thành công")
    @PreAuthorize("hasAuthority('GET /api/orders')")
    @Operation(
            summary = "Lấy danh sách tất cả đơn hàng cho admin với filtering",
            description = "Yêu cầu quyền: <b>GET /api/orders</b>. Hỗ trợ filtering qua query string. Ví dụ: ?userId=1&status=PENDING&totalAmount>1000&createdAt>2024-01-01&page=0&size=20&sort=createdAt,desc"
    )
    public ResponseEntity<PageResponseDto<OrderResponse>> getAllOrders(
            @Filter Specification<Order> spec,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDto<OrderResponse> orders = orderService.getAllOrders(spec, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @ApiMessage(value = "Cập nhật trạng thái đơn hàng thành công")
    @PreAuthorize("hasAuthority('PUT /api/orders/{id}/status')")
    @Operation(
            summary = "Cập nhật trạng thái đơn hàng (Admin)",
            description = "Yêu cầu quyền: <b>PUT /api/orders/{id}/status</b>"
    )
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request) {
        OrderResponse order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    @ApiMessage(value = "Hủy đơn hàng thành công")
    @Operation(summary = "Hủy đơn hàng")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        Long userId = JwtUtil.getCurrentUserId();
        orderService.cancelOrder(id, userId);
        return ResponseEntity.ok().build();
    }
}
