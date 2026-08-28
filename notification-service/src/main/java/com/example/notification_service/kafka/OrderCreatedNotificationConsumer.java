package com.example.notification_service.kafka;

import com.ecommerce.common.events.OrderCreatedEvent;
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
public class OrderCreatedNotificationConsumer {

    private final NotificationService notificationService;

    private final EventIdempotencyService eventIdempotencyService;

    private final NotificationMetrics notificationMetrics;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "notification-order-created-group"
    )
    public void consume(OrderCreatedEvent event) {

        validateEvent(event);

        log.info(
                "Received ORDER_CREATED event: eventId={}, orderId={}",
                event.getEventId(),
                event.getOrderId()
        );

        if (eventIdempotencyService.alreadyProcessed(event)) {

            notificationMetrics.duplicateEvent();

            log.info(
                    "Ignoring already processed ORDER_CREATED event: " +
                            "eventId={}, orderId={}",
                    event.getEventId(),
                    event.getOrderId()
            );

            return;
        }

        notificationService.handleOrderCreated(event);

        markProcessed(event);
    }

    private void markProcessed(OrderCreatedEvent event) {

        eventIdempotencyService.markProcessed(event);
    }

    private void validateEvent(OrderCreatedEvent event) {

        if (event == null) {
            throw new InvalidEventException();
        }

        if (event.getEventId() == null) {
            throw new MissingEventIdException();
        }

        if (event.getEventType() == null) {
            throw new MissingEventTypeException();
        }

        if (event.getOrderId() == null) {
            throw new MissingOrderIdException();
        }

        if (event.getCustomerId() == null) {
            throw new MissingCustomerIdException();
        }
    }


    @DltHandler
    public void handleDeadLetter(OrderCreatedEvent event) {

        log.error(
                "ORDER_CREATED notification event moved to DLT: " +
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