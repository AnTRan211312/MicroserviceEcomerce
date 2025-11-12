package com.ecomerce.orderservice.service;

import com.ecomerce.orderservice.dto.request.OrderCreateRequest;
import com.ecomerce.orderservice.dto.request.OrderUpdateRequest;
import com.ecomerce.orderservice.dto.response.OrderResponse;
import com.ecomerce.orderservice.dto.response.PageResponseDto;
import com.ecomerce.orderservice.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface OrderService {
    OrderResponse createOrder(Long userId, OrderCreateRequest request);
    
    OrderResponse buyNow(Long userId, com.ecomerce.orderservice.dto.request.BuyNowRequest request);
    
    OrderResponse getOrderById(Long orderId, Long userId);
    
    OrderResponse getOrderByOrderNumber(String orderNumber, Long userId);
    
    PageResponseDto<OrderResponse> getUserOrders(Long userId, Specification<Order> spec, Pageable pageable);
    
    PageResponseDto<OrderResponse> getAllOrders(Specification<Order> spec, Pageable pageable);
    
    OrderResponse updateOrderStatus(Long orderId, OrderUpdateRequest request);
    
    void cancelOrder(Long orderId, Long userId);
}

