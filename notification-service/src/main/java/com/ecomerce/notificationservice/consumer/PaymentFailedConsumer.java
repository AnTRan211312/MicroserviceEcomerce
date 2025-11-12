package com.ecomerce.notificationservice.consumer;

import com.ecomerce.notificationservice.event.PaymentFailedEvent;
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
public class PaymentFailedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "payment-failed",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentFailedEvent(
            @Payload PaymentFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("📨 Received PaymentFailedEvent from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Event details: paymentId={}, orderId={}, userId={}, failureReason={}",
                    event.getPaymentId(), event.getOrderId(), event.getUserId(), event.getFailureReason());

            // Send notification
            notificationService.sendPaymentFailedNotification(event);

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed PaymentFailedEvent for payment: {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("❌ Error processing PaymentFailedEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

