package com.example.notification_service.kafka;

import com.ecommerce.common.events.OrderCancelledEvent;
import com.ecommerce.common.exception.*;
import com.ecommerce.common.kafka.EventIdempotencyService;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.notification_service.metrics.NotificationMetrics;
import com.example.notification_service.service.impl.NotificationService;
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
public class OrderCancelledNotificationConsumer {

    private final NotificationService notificationService;

    private final EventIdempotencyService eventIdempotencyService;

    private final NotificationMetrics notificationMetrics;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            groupId = "notification-order-cancelled-group"
    )
    public void consume(OrderCancelledEvent event) {

        validateEvent(event);

        log.info(
                "Received ORDER_CANCELLED event: eventId={}, orderId={}",
                event.getEventId(),
                event.getOrderId()
        );

        if (eventIdempotencyService.alreadyProcessed(event)) {

            notificationMetrics.duplicateEvent();

            return;
        }

        notificationService.handleOrderCancelled(event);

        eventIdempotencyService.markProcessed(event);
    }

    private void validateEvent(OrderCancelledEvent event) {

        if (event == null) {
            throw new InvalidEventException();
        }

        if (event.getEventId() == null) {
            throw new MissingEventIdException();
        }

        if (event.getOrderId() == null) {
            throw new MissingOrderIdException();
        }

        if (event.getCustomerId() == null) {
            throw new MissingCustomerIdException();
        }
    }


    @DltHandler
    public void handleDeadLetter(OrderCancelledEvent event) {

        log.error(
                "ORDER_CANCELLED notification event moved to DLT: " +
                        "eventId={}, orderId={}",
                event != null
                        ? event.getEventId()
                        : null,
                event != null
                        ? event.getOrderId()
                        : null
        );
    }
}