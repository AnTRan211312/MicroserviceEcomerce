package com.ecomerce.paymentservice.service.impl;

import com.ecomerce.paymentservice.event.PaymentCreatedEvent;
import com.ecomerce.paymentservice.event.PaymentFailedEvent;
import com.ecomerce.paymentservice.event.PaymentSuccessEvent;
import com.ecomerce.paymentservice.service.KafkaProducerService;
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

    private static final String PAYMENT_CREATED_TOPIC = "payment-created";
    private static final String PAYMENT_SUCCESS_TOPIC = "payment-success";
    private static final String PAYMENT_FAILED_TOPIC = "payment-failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishPaymentCreatedEvent(PaymentCreatedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(PAYMENT_CREATED_TOPIC, event.getVnpayTxnRef(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("✅ Published PaymentCreatedEvent to topic '{}' for payment: {} (order: {})",
                            PAYMENT_CREATED_TOPIC, event.getPaymentId(), event.getOrderId());
                } else {
                    log.error("❌ Failed to publish PaymentCreatedEvent for payment: {} (order: {})",
                            event.getPaymentId(), event.getOrderId(), exception);
                }
            });
        } catch (Exception e) {
            log.error("❌ Error publishing PaymentCreatedEvent for payment: {} (order: {})",
                    event.getPaymentId(), event.getOrderId(), e);
        }
    }

    @Override
    public void publishPaymentSuccessEvent(PaymentSuccessEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(PAYMENT_SUCCESS_TOPIC, event.getVnpayTxnRef(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("✅ Published PaymentSuccessEvent to topic '{}' for payment: {} (order: {})",
                            PAYMENT_SUCCESS_TOPIC, event.getPaymentId(), event.getOrderId());
                } else {
                    log.error("❌ Failed to publish PaymentSuccessEvent for payment: {} (order: {})",
                            event.getPaymentId(), event.getOrderId(), exception);
                }
            });
        } catch (Exception e) {
            log.error("❌ Error publishing PaymentSuccessEvent for payment: {} (order: {})",
                    event.getPaymentId(), event.getOrderId(), e);
        }
    }

    @Override
    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(PAYMENT_FAILED_TOPIC, event.getVnpayTxnRef(), event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("✅ Published PaymentFailedEvent to topic '{}' for payment: {} (order: {})",
                            PAYMENT_FAILED_TOPIC, event.getPaymentId(), event.getOrderId());
                } else {
                    log.error("❌ Failed to publish PaymentFailedEvent for payment: {} (order: {})",
                            event.getPaymentId(), event.getOrderId(), exception);
                }
            });
        } catch (Exception e) {
            log.error("❌ Error publishing PaymentFailedEvent for payment: {} (order: {})",
                    event.getPaymentId(), event.getOrderId(), e);
        }
    }
}

