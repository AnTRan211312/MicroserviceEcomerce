package com.ecomerce.notificationservice.service;

import com.ecomerce.notificationservice.dto.response.NotificationResponse;
import com.ecomerce.notificationservice.dto.response.PageResponseDto;
import com.ecomerce.notificationservice.event.CartItemAddedEvent;
import com.ecomerce.notificationservice.event.OrderCreatedEvent;
import com.ecomerce.notificationservice.event.OrderStatusChangedEvent;
import com.ecomerce.notificationservice.event.PaymentFailedEvent;
import com.ecomerce.notificationservice.event.PaymentSuccessEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    void sendCartItemAddedNotification(CartItemAddedEvent event);
    
    void sendOrderCreatedNotification(OrderCreatedEvent event);
    
    void sendOrderStatusChangedNotification(OrderStatusChangedEvent event);
    
    void sendPaymentSuccessNotification(PaymentSuccessEvent event);
    
    void sendPaymentFailedNotification(PaymentFailedEvent event);
    
    PageResponseDto<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);
    
    List<NotificationResponse> getUnreadNotifications(Long userId);
    
    Long getUnreadCount(Long userId);
    
    void markAsRead(Long notificationId, Long userId);
    
    void markAllAsRead(Long userId);
}

