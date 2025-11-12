package com.ecomerce.notificationservice.consumer;

import com.ecomerce.notificationservice.event.CartItemAddedEvent;
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
public class CartItemAddedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "cart-item-added",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCartItemAddedEvent(
            @Payload CartItemAddedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("📨 Received CartItemAddedEvent from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Event details: userId={}, productId={}, productName={}, quantity={}",
                    event.getUserId(), event.getProductId(), event.getProductName(), event.getQuantity());

            // Send notification
            notificationService.sendCartItemAddedNotification(event);

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed CartItemAddedEvent for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Error processing CartItemAddedEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

