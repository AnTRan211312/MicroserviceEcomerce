package com.ecomerce.orderservice.consumer;

import com.ecomerce.orderservice.event.OrderStatusChangedEvent;
import com.ecomerce.orderservice.event.PaymentFailedEvent;
import com.ecomerce.orderservice.event.PaymentSuccessEvent;
import com.ecomerce.orderservice.model.Order;
import com.ecomerce.orderservice.repository.OrderRepository;
import com.ecomerce.orderservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Kafka Consumer để consume Payment events từ payment-service
 * - PaymentSuccessEvent: Update order status từ PENDING -> CONFIRMED
 * - PaymentFailedEvent: Log và có thể thông báo cho user
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusChangedConsumer {

    private final OrderRepository orderRepository;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(
            topics = "payment-success",
            groupId = "order-service-group",
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

            // Get order
            Order order = orderRepository.findByIdWithItems(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            // Validate order belongs to user
            if (!order.getUserId().equals(event.getUserId())) {
                log.error("❌ Order {} does not belong to user {}", event.getOrderId(), event.getUserId());
                throw new RuntimeException("Order does not belong to user");
            }

            // Validate order status - chỉ update nếu order đang ở PENDING
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                log.warn("⚠️ Order {} is not in PENDING status. Current status: {}. Skipping status update.", 
                        event.getOrderId(), order.getStatus());
            } else {
                Order.OrderStatus oldStatus = order.getStatus();
                // Update order status to CONFIRMED
                order.setStatus(Order.OrderStatus.CONFIRMED);
                orderRepository.save(order);
                log.info("✅ Updated order {} status from PENDING to CONFIRMED after successful payment", 
                        event.getOrderId());
                
                // Publish OrderStatusChangedEvent to Kafka
                OrderStatusChangedEvent statusEvent = OrderStatusChangedEvent.builder()
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .orderNumber(order.getOrderNumber())
                        .oldStatus(oldStatus.name())
                        .newStatus(Order.OrderStatus.CONFIRMED.name())
                        .items(order.getItems().stream()
                                .map(item -> OrderStatusChangedEvent.OrderItemEvent.builder()
                                        .productId(item.getProductId())
                                        .productName(item.getProductName())
                                        .price(item.getPrice())
                                        .quantity(item.getQuantity())
                                        .build())
                                .collect(Collectors.toList()))
                        .timestamp(Instant.now())
                        .build();
                
                kafkaProducerService.publishOrderStatusChangedEvent(statusEvent);
                log.info("✅ Published OrderStatusChangedEvent for order: {}", order.getOrderNumber());
            }

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed PaymentSuccessEvent for order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error processing PaymentSuccessEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "order-service-group",
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

            // Get order
            Order order = orderRepository.findByIdWithItems(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            // Validate order belongs to user
            if (!order.getUserId().equals(event.getUserId())) {
                log.error("❌ Order {} does not belong to user {}", event.getOrderId(), event.getUserId());
                throw new RuntimeException("Order does not belong to user");
            }

            // Log payment failure - order status vẫn giữ nguyên PENDING
            // User có thể thử thanh toán lại
            log.warn("⚠️ Payment failed for order: {}. Reason: {}. Order status remains PENDING.", 
                    event.getOrderId(), event.getFailureReason());

            // Acknowledge message
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed PaymentFailedEvent for order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error processing PaymentFailedEvent: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}

