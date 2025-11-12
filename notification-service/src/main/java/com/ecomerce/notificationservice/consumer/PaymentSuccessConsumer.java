package com.ecomerce.notificationservice.consumer;

import com.ecomerce.notificationservice.event.PaymentSuccessEvent;
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
public class PaymentSuccessConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "payment-success",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentSuccessEvent(
            @Payload PaymentSuccessEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("📨 Received PaymentSuccessEvent from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Event details: paymentId={}, orderId={}, userId={}, amount={}",
                    event.getPaymentId(), event.getOrderId(), event.getUserId(), event.getAmount());

            // Send notification
            notificationService.sendPaymentSuccessNotification(event);

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed PaymentSuccessEvent for payment: {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("❌ Error processing PaymentSuccessEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

