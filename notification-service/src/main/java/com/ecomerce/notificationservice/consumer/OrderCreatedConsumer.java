package com.ecomerce.notificationservice.consumer;

import com.ecomerce.notificationservice.event.OrderCreatedEvent;
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
public class OrderCreatedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order-created",
            groupId = "notification-service-group",
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
            log.info("Event details: orderId={}, orderNumber={}, userId={}, totalAmount={}",
                    event.getOrderId(), event.getOrderNumber(), event.getUserId(), event.getTotalAmount());

            // Send notification
            notificationService.sendOrderCreatedNotification(event);

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed OrderCreatedEvent for order: {}", event.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Error processing OrderCreatedEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

