package com.example.order_service.kafka;

import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.order_service.Service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "order-group")
    public void consume(PaymentCompletedEvent event) {

        log.info("Received PaymentCompletedEvent : {}", event);

        orderService.handlePaymentCompleted(event);

    }
}
