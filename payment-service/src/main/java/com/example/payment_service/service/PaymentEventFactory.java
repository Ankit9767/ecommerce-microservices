package com.example.payment_service.service;

import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.kafka.EventType;
import com.example.payment_service.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventFactory {

    public PaymentCompletedEvent createPaymentEvent(EventType eventType,
                                                    Payment payment) {

        return PaymentCompletedEvent.builder()
                .eventType(eventType)
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getStatus())
                .transactionId(payment.getProviderReference())
                .failureReason(payment.getFailureReason())
                .build();
    }
}