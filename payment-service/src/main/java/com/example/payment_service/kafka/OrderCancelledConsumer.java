package com.example.payment_service.kafka;

import com.ecommerce.common.events.OrderCancelledEvent;
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
public class OrderCancelledConsumer {

    private final PaymentService paymentService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            groupId = "payment-cancellation-group"
    )
    public void consume(OrderCancelledEvent event) {

        log.info(
                "Received ORDER_CANCELLED event for order {}, reason={}",
                event.getOrderId(),
                event.getReason()
        );

        if (event.getEventType() != EventType.ORDER_CANCELLED) {

            log.debug(
                    "Ignoring unexpected event type {}",
                    event.getEventType()
            );

            return;
        }

        paymentService.processOrderCancelledEvent(event);
    }

    @DltHandler
    public void handleDeadLetter(OrderCancelledEvent event) {

        log.error(
                "Order-cancelled event moved to DLT after retries exhausted: {}",
                event
        );
    }
}