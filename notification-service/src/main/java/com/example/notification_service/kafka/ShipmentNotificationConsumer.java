package com.example.notification_service.kafka;

import com.ecommerce.common.events.ShipmentCancelledEvent;
import com.ecommerce.common.events.ShipmentCreatedEvent;
import com.ecommerce.common.events.ShipmentDeliveredEvent;
import com.ecommerce.common.events.ShipmentFailedEvent;
import com.ecommerce.common.events.ShipmentInTransitEvent;
import com.ecommerce.common.events.ShipmentOutForDeliveryEvent;
import com.ecommerce.common.events.ShipmentEvent;
import com.ecommerce.common.events.ShipmentShippedEvent;
import com.ecommerce.common.exception.*;
import com.ecommerce.common.kafka.EventIdempotencyService;
import com.ecommerce.common.kafka.EventType;
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
public class ShipmentNotificationConsumer {

    private final NotificationService notificationService;

    private final EventIdempotencyService eventIdempotencyService;

    private final NotificationMetrics notificationMetrics;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.SHIPMENT_EVENTS,
            groupId = "notification-shipment-group"
    )
    public void consume(ShipmentEvent event) {

        validateEvent(event);

        log.info(
                "Received shipment event: " +
                        "eventId={}, eventType={}, " +
                        "shipmentId={}, orderId={}, customerId={}",
                event.getEventId(),
                event.getEventType(),
                event.getShipmentId(),
                event.getOrderId(),
                event.getCustomerId()
        );

        if (eventIdempotencyService.alreadyProcessed(event)) {

            notificationMetrics.duplicateEvent();

            log.info(
                    "Ignoring already processed shipment event: " +
                            "eventId={}, eventType={}",
                    event.getEventId(),
                    event.getEventType()
            );

            return;
        }

        handleEvent(event);

        eventIdempotencyService.markProcessed(event);
    }

    private void handleEvent(ShipmentEvent event) {

        EventType eventType = event.getEventType();

        switch (eventType) {

            case SHIPMENT_CREATED ->

                    notificationService.handleShipmentCreated(
                            (ShipmentCreatedEvent) event
                    );

            case SHIPMENT_SHIPPED ->

                    notificationService.handleShipmentShipped(
                            (ShipmentShippedEvent) event
                    );

            case SHIPMENT_IN_TRANSIT ->

                    notificationService.handleShipmentInTransit(
                            (ShipmentInTransitEvent) event
                    );

            case SHIPMENT_OUT_FOR_DELIVERY ->

                    notificationService.handleShipmentOutForDelivery(
                            (ShipmentOutForDeliveryEvent) event
                    );

            case SHIPMENT_DELIVERED ->

                    notificationService.handleShipmentDelivered(
                            (ShipmentDeliveredEvent) event
                    );

            case SHIPMENT_FAILED ->

                    notificationService.handleShipmentFailed(
                            (ShipmentFailedEvent) event
                    );

            case SHIPMENT_CANCELLED ->

                    notificationService.handleShipmentCancelled(
                            (ShipmentCancelledEvent) event
                    );

            default ->

                    log.warn(
                            "Ignoring unsupported shipment event type: {}",
                            eventType
                    );
        }
    }

    private void validateEvent(ShipmentEvent event) {

        if (event == null) {

            throw new InvalidEventException();
        }

        if (event.getEventId() == null) {

            throw new MissingEventIdException();
        }

        if (event.getEventType() == null) {

            throw new MissingEventTypeException();
        }

        if (event.getShipmentId() == null) {

            throw new MissingShipmentIdException();
        }

        if (event.getOrderId() == null) {

            throw new MissingOrderIdException();
        }

        if (event.getCustomerId() == null) {

            throw new MissingCustomerIdException();
        }
    }

    @DltHandler
    public void handleDeadLetter(ShipmentEvent event) {

        log.error(
                "Shipment notification event moved to DLT: " +
                        "eventId={}, eventType={}, shipmentId={}, " +
                        "orderId={}, customerId={}",
                event != null
                        ? event.getEventId()
                        : null,
                event != null
                        ? event.getEventType()
                        : null,
                event != null
                        ? event.getShipmentId()
                        : null,
                event != null
                        ? event.getOrderId()
                        : null,
                event != null
                        ? event.getCustomerId()
                        : null
        );
    }
}