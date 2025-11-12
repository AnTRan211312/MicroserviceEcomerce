package com.ecomerce.cartservice.controller;

import com.ecomerce.cartservice.annontation.ApiMessage;
import com.ecomerce.cartservice.dto.request.CartItemRequest;
import com.ecomerce.cartservice.dto.request.CartItemUpdateRequest;
import com.ecomerce.cartservice.dto.request.CheckoutRequest;
import com.ecomerce.cartservice.dto.response.ApiResponse;
import com.ecomerce.cartservice.dto.response.CartItemResponse;
import com.ecomerce.cartservice.dto.response.CartResponse;
import com.ecomerce.cartservice.dto.response.CheckoutResponse;
import com.ecomerce.cartservice.dto.response.PageResponseDto;
import com.ecomerce.cartservice.model.Cart;
import com.ecomerce.cartservice.service.CartService;
import com.ecomerce.cartservice.util.JwtUtil;
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

@Tag(name = "Cart", description = "Quản lý giỏ hàng")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @ApiMessage(value = "Lấy giỏ hàng thành công")
    @Operation(summary = "Lấy giỏ hàng của user hiện tại")
    public ResponseEntity<CartResponse> getMyCart() {
        Long userId = JwtUtil.getCurrentUserId();
        CartResponse cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    @ApiMessage(value = "Thêm sản phẩm vào giỏ hàng thành công")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody CartItemRequest request
    ) {
        Long userId = JwtUtil.getCurrentUserId();
        CartResponse cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    @ApiMessage(value = "Cập nhật số lượng sản phẩm thành công")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        Long userId = JwtUtil.getCurrentUserId();
        CartResponse cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    @ApiMessage(value = "Xóa sản phẩm khỏi giỏ hàng thành công")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable Long itemId
    ) {
        Long userId = JwtUtil.getCurrentUserId();
        CartResponse cart = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    @ApiMessage(value = "Xóa toàn bộ giỏ hàng thành công")
    @Operation(summary = "Xóa toàn bộ giỏ hàng")
    public ResponseEntity<CartResponse> clearCart() {
        Long userId = JwtUtil.getCurrentUserId();
        CartResponse cart = cartService.clearCart(userId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/items/{itemId}")
    @ApiMessage(value = "Lấy thông tin sản phẩm trong giỏ hàng thành công")
    @Operation(summary = "Lấy thông tin chi tiết một sản phẩm trong giỏ hàng")
    public ResponseEntity<CartItemResponse> getCartItemById(@PathVariable Long itemId) {
        Long userId = JwtUtil.getCurrentUserId();
        CartItemResponse item = cartService.getCartItemById(userId, itemId);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/checkout")
    @ApiMessage(value = "Checkout thành công")
    @Operation(summary = "Checkout một hoặc nhiều sản phẩm từ giỏ hàng để tạo đơn hàng")
    public ResponseEntity<CheckoutResponse> checkout(
            @Valid @RequestBody CheckoutRequest request
    ) {
        Long userId = JwtUtil.getCurrentUserId();
        CheckoutResponse response = cartService.checkout(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách tất cả giỏ hàng cho admin với filtering (PROTECTED - cần permission GET /api/carts/admin/all)
     * Hỗ trợ filtering qua query string: ?userId=1&isActive=true&totalAmount>1000&createdAt>2024-01-01
     * Hỗ trợ pagination: ?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/admin/all")
    @ApiMessage(value = "Lấy danh sách tất cả giỏ hàng thành công")
    @PreAuthorize("hasAuthority('GET /api/carts/admin/all')")
    @Operation(
            summary = "Lấy danh sách tất cả giỏ hàng cho admin với filtering",
            description = "Yêu cầu quyền: <b>GET /api/carts/admin/all</b>. Hỗ trợ filtering qua query string. Ví dụ: ?userId=1&isActive=true&totalAmount>1000&createdAt>2024-01-01&page=0&size=20&sort=createdAt,desc"
    )
    public ResponseEntity<ApiResponse<PageResponseDto<CartResponse>>> getAllCarts(
            @Filter Specification<Cart> spec,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDto<CartResponse> carts = cartService.getAllCarts(spec, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách tất cả giỏ hàng thành công", null, carts));
    }
}
