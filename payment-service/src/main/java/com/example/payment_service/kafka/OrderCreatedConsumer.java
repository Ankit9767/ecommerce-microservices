package com.example.payment_service.kafka;

import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "payment-group"
    )
    public void consume(OrderEvent event) {

        log.info("Received OrderEvent [{}] for order {}", event.getEventType(),
                event.getOrderId());

        if (event.getEventType() != EventType.ORDER_CREATED) {

            log.debug("Ignoring OrderEvent type {} for payment processing",
                    event.getEventType());

            return;
        }

        paymentService.processOrderEvent(event);
    }

    @DltHandler
    public void handleDeadLetter(OrderEvent event) {

        log.error("Order event moved to DLT after retries exhausted : {}", event);
    }
}