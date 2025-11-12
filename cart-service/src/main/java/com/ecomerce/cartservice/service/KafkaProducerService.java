package com.ecomerce.cartservice.service;

import com.ecomerce.cartservice.event.CartItemAddedEvent;

public interface KafkaProducerService {
    void publishCartItemAddedEvent(CartItemAddedEvent event);
}
