package com.ecomerce.notificationservice.consumer;

import com.ecomerce.notificationservice.event.OrderStatusChangedEvent;
import com.ecomerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order-status-changed",
            groupId = "notification-service-group",
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
            log.info("Event details: orderId={}, orderNumber={}, oldStatus={}, newStatus={}",
                    event.getOrderId(), event.getOrderNumber(), 
                    event.getOldStatus(), event.getNewStatus());

            // Send notification
            notificationService.sendOrderStatusChangedNotification(event);

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed OrderStatusChangedEvent for order: {}", event.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Error processing OrderStatusChangedEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

