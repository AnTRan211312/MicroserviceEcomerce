package com.ecomerce.paymentservice.service;

import com.ecomerce.paymentservice.event.PaymentCreatedEvent;
import com.ecomerce.paymentservice.event.PaymentFailedEvent;
import com.ecomerce.paymentservice.event.PaymentSuccessEvent;

public interface KafkaProducerService {
    void publishPaymentCreatedEvent(PaymentCreatedEvent event);
    void publishPaymentSuccessEvent(PaymentSuccessEvent event);
    void publishPaymentFailedEvent(PaymentFailedEvent event);
}

