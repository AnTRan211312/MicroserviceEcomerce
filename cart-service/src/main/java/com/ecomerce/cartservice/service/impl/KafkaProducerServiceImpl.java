package com.ecomerce.cartservice.service.impl;

import com.ecomerce.cartservice.event.CartItemAddedEvent;
import com.ecomerce.cartservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final String CART_ITEM_ADDED_TOPIC = "cart-item-added";

    private final KafkaTemplate<String, CartItemAddedEvent> kafkaTemplate;

    @Override
    public void publishCartItemAddedEvent(CartItemAddedEvent event) {
        try {
            CompletableFuture<SendResult<String, CartItemAddedEvent>> future = 
                    kafkaTemplate.send(CART_ITEM_ADDED_TOPIC, event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("✅ Published CartItemAddedEvent to topic '{}': userId={}, productId={}, quantity={}",
                            CART_ITEM_ADDED_TOPIC, event.getUserId(), event.getProductId(), event.getQuantity());
                } else {
                    log.error("❌ Failed to publish CartItemAddedEvent to topic '{}': {}",
                            CART_ITEM_ADDED_TOPIC, exception.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("❌ Error publishing CartItemAddedEvent: {}", e.getMessage(), e);
        }
    }
}

