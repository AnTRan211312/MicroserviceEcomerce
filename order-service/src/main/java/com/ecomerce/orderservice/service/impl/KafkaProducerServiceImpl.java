package com.ecomerce.orderservice.service.impl;

import com.ecomerce.orderservice.event.OrderCreatedEvent;
import com.ecomerce.orderservice.event.OrderStatusChangedEvent;
import com.ecomerce.orderservice.service.KafkaProducerService;
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

    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_STATUS_CHANGED_TOPIC = "order-status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            log.info("📤 Publishing OrderCreatedEvent to topic '{}' for order: {} with {} items",
                    ORDER_CREATED_TOPIC, event.getOrderNumber(), event.getItems().size());
            log.debug("Event details: orderId={}, userId={}, items={}", 
                    event.getOrderId(), event.getUserId(), event.getItems());
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_CREATED_TOPIC, event.getOrderNumber(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("✅ Successfully published OrderCreatedEvent to topic '{}' for order: {} at partition {}, offset {}",
                            ORDER_CREATED_TOPIC, event.getOrderNumber(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to publish OrderCreatedEvent for order: {} to topic '{}'. Error: {}",
                            event.getOrderNumber(), ORDER_CREATED_TOPIC, exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            log.error("❌ Error publishing OrderCreatedEvent for order: {} to topic '{}'. Error: {}",
                    event.getOrderNumber(), ORDER_CREATED_TOPIC, e.getMessage(), e);
        }
    }

    @Override
    public void publishOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_STATUS_CHANGED_TOPIC, event.getOrderNumber(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("✅ Published OrderStatusChangedEvent to topic '{}' for order: {}",
                            ORDER_STATUS_CHANGED_TOPIC, event.getOrderNumber());
                } else {
                    log.error("❌ Failed to publish OrderStatusChangedEvent for order: {}",
                            event.getOrderNumber(), exception);
                }
            });
        } catch (Exception e) {
            log.error("❌ Error publishing OrderStatusChangedEvent for order: {}",
                    event.getOrderNumber(), e);
        }
    }
}

