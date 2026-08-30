package com.example.shipping_service.kafka;

import com.ecommerce.common.events.OrderPaidEvent;
import com.ecommerce.common.exception.*;
import com.ecommerce.common.kafka.EventIdempotencyService;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.shipping_service.service.ShipmentService;
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
public class OrderPaidConsumer {

    private final ShipmentService shipmentService;

    private final EventIdempotencyService eventIdempotencyService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_PAID,
            groupId = "shipping-group"
    )
    public void consume(OrderPaidEvent event) {

        validateEvent(event);

        log.info(
                "Received OrderPaidEvent: eventId={}, " +
                        "eventType={}, orderId={}, customerId={}",
                event.getEventId(),
                event.getEventType(),
                event.getOrderId(),
                event.getCustomerId()
        );

        if (eventIdempotencyService.alreadyProcessed(event)) {

            log.info(
                    "Ignoring already processed OrderPaidEvent: " +
                            "eventId={}, orderId={}",
                    event.getEventId(),
                    event.getOrderId()
            );

            return;
        }

        if (event.getEventType() != EventType.ORDER_PAID) {

            log.warn(
                    "Ignoring unexpected event type: " +
                            "eventId={}, eventType={}, orderId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getOrderId()
            );

            return;
        }

        shipmentService.createShipment(event);

        markProcessed(event);
    }

    private void markProcessed(OrderPaidEvent event) {

        boolean marked =
                eventIdempotencyService.markProcessed(event);

        if (!marked) {

            log.info(
                    "OrderPaidEvent was processed concurrently: " +
                            "eventId={}, orderId={}",
                    event.getEventId(),
                    event.getOrderId()
            );
        }
    }

    private void validateEvent(OrderPaidEvent event) {

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
    public void handleDeadLetter(OrderPaidEvent event) {

        log.error(
                "OrderPaidEvent moved to DLT after retries exhausted: " +
                        "eventId={}, eventType={}, orderId={}, " +
                        "customerId={}, event={}",
                event != null
                        ? event.getEventId()
                        : null,
                event != null
                        ? event.getEventType()
                        : null,
                event != null
                        ? event.getOrderId()
                        : null,
                event != null
                        ? event.getCustomerId()
                        : null,
                event
        );
    }
}