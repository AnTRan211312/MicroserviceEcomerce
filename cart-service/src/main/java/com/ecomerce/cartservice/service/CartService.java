package com.ecomerce.cartservice.service;

import com.ecomerce.cartservice.dto.request.CartItemRequest;
import com.ecomerce.cartservice.dto.request.CartItemUpdateRequest;
import com.ecomerce.cartservice.dto.request.CheckoutRequest;
import com.ecomerce.cartservice.dto.response.CartItemResponse;
import com.ecomerce.cartservice.dto.response.CartResponse;
import com.ecomerce.cartservice.dto.response.CheckoutResponse;
import com.ecomerce.cartservice.dto.response.PageResponseDto;
import com.ecomerce.cartservice.model.Cart;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface CartService {
    
    CartResponse getCartByUserId(Long userId);
    
    CartResponse addItemToCart(Long userId, CartItemRequest request);
    
    CartResponse updateCartItem(Long userId, Long itemId, CartItemUpdateRequest request);
    
    CartResponse removeItemFromCart(Long userId, Long itemId);
    
    CartResponse clearCart(Long userId);
    
    void deleteCart(Long userId);
    
    PageResponseDto<CartResponse> getAllCarts(Specification<Cart> spec, Pageable pageable);
    
    CartItemResponse getCartItemById(Long userId, Long itemId);
    
    CheckoutResponse checkout(Long userId, CheckoutRequest request);
}

