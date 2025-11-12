package com.ecomerce.inventoryservice.consumer;

import com.ecomerce.inventoryservice.event.OrderStatusChangedEvent;
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
 * Kafka Consumer để consume OrderStatusChangedEvent
 * - Khi order bị CANCELLED: release reserved quantity (trả lại số lượng đã reserve)
 * - Khi order DELIVERED: deduct quantity từ inventory (trừ số lượng đã reserve khỏi quantity thực tế)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangedConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-status-changed",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderStatusChangedEvent(
            @Payload OrderStatusChangedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("📨 Received OrderStatusChangedEvent from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Event details: orderId={}, orderNumber={}, oldStatus={}, newStatus={}, itemsCount={}",
                    event.getOrderId(), event.getOrderNumber(), 
                    event.getOldStatus(), event.getNewStatus(),
                    event.getItems() != null ? event.getItems().size() : 0);

            // Validate event has items
            if (event.getItems() == null || event.getItems().isEmpty()) {
                log.warn("⚠️ OrderStatusChangedEvent for order {} has no items. Skipping inventory update.", 
                        event.getOrderNumber());
                acknowledgment.acknowledge();
                return;
            }

            // Xử lý dựa trên status mới
            if ("CANCELLED".equals(event.getNewStatus())) {
                log.info("🔄 Order cancelled: {} - Releasing reserved quantity for {} items", 
                        event.getOrderNumber(), event.getItems().size());
                
                // Release reserved quantity cho từng item
                for (OrderStatusChangedEvent.OrderItemEvent item : event.getItems()) {
                    try {
                        log.info("🔄 Releasing {} reserved units for productId: {}", 
                                item.getQuantity(), item.getProductId());
                        inventoryService.releaseReservedQuantity(item.getProductId(), item.getQuantity());
                        log.info("✅ Successfully released {} reserved units for productId: {}", 
                                item.getQuantity(), item.getProductId());
                    } catch (Exception e) {
                        log.error("❌ Failed to release reserved quantity for productId: {}, quantity: {}. Error: {}", 
                                item.getProductId(), item.getQuantity(), e.getMessage(), e);
                        // Continue với item tiếp theo thay vì fail toàn bộ order
                    }
                }
                
            } else if ("DELIVERED".equals(event.getNewStatus())) {
                log.info("📦 Order delivered: {} - Deducting quantity for {} items", 
                        event.getOrderNumber(), event.getItems().size());
                
                // Deduct quantity cho từng item (đã được reserve, giờ trừ khỏi quantity thực tế)
                for (OrderStatusChangedEvent.OrderItemEvent item : event.getItems()) {
                    try {
                        log.info("📦 Deducting {} units for productId: {}", 
                                item.getQuantity(), item.getProductId());
                        inventoryService.deductQuantity(item.getProductId(), item.getQuantity());
                        log.info("✅ Successfully deducted {} units for productId: {}", 
                                item.getQuantity(), item.getProductId());
                    } catch (Exception e) {
                        log.error("❌ Failed to deduct quantity for productId: {}, quantity: {}. Error: {}", 
                                item.getProductId(), item.getQuantity(), e.getMessage(), e);
                        // Continue với item tiếp theo thay vì fail toàn bộ order
                    }
                }
            } else {
                log.debug("ℹ️ Order status changed to {} - No inventory action required for order: {}", 
                        event.getNewStatus(), event.getOrderNumber());
            }

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed OrderStatusChangedEvent for order: {}", 
                    event.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Error processing OrderStatusChangedEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

