package com.ecomerce.orderservice.service;

import com.ecomerce.orderservice.event.OrderCreatedEvent;
import com.ecomerce.orderservice.event.OrderStatusChangedEvent;

public interface KafkaProducerService {
    void publishOrderCreatedEvent(OrderCreatedEvent event);
    
    void publishOrderStatusChangedEvent(OrderStatusChangedEvent event);
}

