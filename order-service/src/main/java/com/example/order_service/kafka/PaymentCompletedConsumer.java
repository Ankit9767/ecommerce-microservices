package com.example.order_service.kafka;

import com.ecommerce.common.events.PaymentEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.order_service.service.OrderService;
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
public class PaymentCompletedConsumer {

    private final OrderService orderService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "order-group"
    )
    public void consume(PaymentEvent event) {

        log.info("Received PaymentEvent [{}] : {}", event.getEventType(), event);

        EventType eventType = event.getEventType();

        if (eventType == null) {

            log.warn("Ignoring payment event with unknown event type for order {}",
                    event.getOrderId());

            return;
        }

        switch (eventType) {
            case PAYMENT_SUCCESSFUL ->
                    orderService.handlePaymentCompleted(event);
            case PAYMENT_FAILED ->
                    log.warn("Payment failed for order {} - no status change",
                            event.getOrderId());
            default ->
                    log.warn("Ignoring unexpected payment event type '{}'",
                            eventType);
        }
    }

    @DltHandler
    public void handleDeadLetter(PaymentEvent event) {

        log.error("Payment event moved to DLT after retries exhausted : {}",
                event);
    }
}