package com.ecomerce.inventoryservice.consumer;

import com.ecomerce.inventoryservice.event.OrderCreatedEvent;
import com.ecomerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer để consume OrderCreatedEvent
 * Reserve quantity khi order được tạo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-created",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("📨 Received OrderCreatedEvent from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Event details: orderId={}, orderNumber={}, userId={}, items={}",
                    event.getOrderId(), event.getOrderNumber(), event.getUserId(), 
                    event.getItems().size());

            // Reserve quantity cho từng item trong order
            for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
                try {
                    log.info("🔄 Attempting to reserve {} units for productId: {}", 
                            item.getQuantity(), item.getProductId());
                    inventoryService.reserveQuantity(item.getProductId(), item.getQuantity());
                    log.info("✅ Successfully reserved {} units for productId: {}", 
                            item.getQuantity(), item.getProductId());
                } catch (Exception e) {
                    log.error("❌ Failed to reserve quantity for productId: {}, quantity: {}. Error: {}", 
                            item.getProductId(), item.getQuantity(), e.getMessage(), e);
                    // Continue với item tiếp theo thay vì fail toàn bộ order
                }
            }

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed OrderCreatedEvent for order: {}", 
                    event.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Error processing OrderCreatedEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

