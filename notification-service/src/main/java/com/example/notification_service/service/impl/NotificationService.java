package com.example.notification_service.service.impl;

import com.ecommerce.common.events.*;
import com.example.notification_service.dto.NotificationProviderRequest;
import com.example.notification_service.dto.NotificationProviderResponse;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.enums.NotificationType;
import com.example.notification_service.metrics.NotificationMetrics;

import com.example.notification_service.service.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationPersistenceService persistenceService;

    private final NotificationProvider notificationProvider;

    private final NotificationMetrics notificationMetrics;

    public void handleShipmentCreated(ShipmentCreatedEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.SHIPMENT_CREATED)
                                .recipient(event.getRecipientEmail())
                                .subject("Shipment Created")
                                .message(
                                        "Your shipment for order #" +
                                                event.getOrderId() +
                                                " has been created."
                                )
                                .build()
                );

        send(notification);
    }


    public void handleShipmentShipped(ShipmentShippedEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.SHIPMENT_SHIPPED)
                                .recipient(event.getRecipientEmail())
                                .subject("Order Shipped")
                                .message(
                                        buildShippedMessage(event)
                                )
                                .build()
                );

        send(notification);
    }


    public void handleShipmentInTransit(ShipmentInTransitEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.SHIPMENT_IN_TRANSIT)
                                .recipient(event.getRecipientEmail())
                                .subject("Shipment In Transit")
                                .message(
                                        "Your order #" +
                                                event.getOrderId() +
                                                " is currently in transit."
                                )
                                .build()
                );

        send(notification);
    }


    public void handleShipmentOutForDelivery(ShipmentOutForDeliveryEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(
                                        NotificationType
                                                .SHIPMENT_OUT_FOR_DELIVERY
                                )
                                .recipient(event.getRecipientEmail())
                                .subject("Out for Delivery")
                                .message(
                                        "Your order #" +
                                                event.getOrderId() +
                                                " is out for delivery."
                                )
                                .build()
                );

        send(notification);
    }


    public void handleShipmentDelivered(ShipmentDeliveredEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.SHIPMENT_DELIVERED)
                                .recipient(event.getRecipientEmail())
                                .subject("Order Delivered")
                                .message(
                                        "Your order #" +
                                                event.getOrderId() +
                                                " has been delivered successfully."
                                )
                                .build()
                );

        send(notification);
    }


    public void handleShipmentFailed(ShipmentFailedEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.SHIPMENT_FAILED)
                                .recipient(event.getRecipientEmail())
                                .subject("Shipment Failed")
                                .message(
                                        "There was a problem delivering " +
                                                "your order #" +
                                                event.getOrderId() +
                                                "."
                                )
                                .build()
                );

        send(notification);
    }


    public void handleShipmentCancelled(ShipmentCancelledEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.SHIPMENT_CANCELLED)
                                .recipient(event.getRecipientEmail())
                                .subject("Shipment Cancelled")
                                .message(
                                        "The shipment for order #" +
                                                event.getOrderId() +
                                                " has been cancelled."
                                )
                                .build()
                );

        send(notification);
    }


    private String buildShippedMessage(ShipmentShippedEvent event) {

        StringBuilder message =
                new StringBuilder(
                        "Your order #" +
                                event.getOrderId() +
                                " has been shipped."
                );

        if (event.getCarrier() != null &&
                !event.getCarrier().isBlank()) {

            message.append(
                    " Carrier: "
            ).append(event.getCarrier());
        }

        if (event.getTrackingNumber() != null &&
                !event.getTrackingNumber().isBlank()) {

            message.append(
                    ". Tracking number: "
            ).append(event.getTrackingNumber());
        }

        return message.toString();
    }

    public void handleOrderCreated(OrderCreatedEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.ORDER_CREATED)
                                .recipient(
                                        event.getRecipientEmail()
                                )
                                .subject("Order Created")
                                .message(
                                        "Your order #" +
                                                event.getOrderId() +
                                                " has been created successfully."
                                )
                                .build()
                );

        send(notification);
    }

    public void handleOrderCancelled(OrderCancelledEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.ORDER_CANCELLED)
                                .recipient(
                                        event.getRecipientEmail()
                                )
                                .subject("Order Cancelled")
                                .message(
                                        "Your order #" +
                                                event.getOrderId() +
                                                " has been cancelled."
                                )
                                .build()
                );

        send(notification);
    }

    public void handlePaymentSuccessful(PaymentCompletedEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(
                                        event.getCustomerId()
                                )
                                .orderId(event.getOrderId())
                                .paymentId(event.getPaymentId())
                                .type(
                                        NotificationType.PAYMENT_SUCCESSFUL
                                )
                                .recipient(
                                        event.getRecipientEmail()
                                )
                                .subject("Payment Successful")
                                .message(
                                        "Payment for order #" +
                                                event.getOrderId() +
                                                " was successful."
                                )
                                .build()
                );

        send(notification);
    }

    public void handlePaymentFailed(PaymentCompletedEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(
                                        event.getCustomerId()
                                )
                                .orderId(event.getOrderId())
                                .paymentId(event.getPaymentId())
                                .type(
                                        NotificationType.PAYMENT_FAILED
                                )
                                .recipient(
                                        event.getRecipientEmail()
                                )
                                .subject("Payment Failed")
                                .message(
                                        "Payment for order #" +
                                                event.getOrderId() +
                                                " could not be completed."
                                )
                                .build()
                );

        send(notification);
    }

    private void send(Notification notification) {

        persistenceService.markProcessing(notification.getId());

        NotificationProviderRequest request =
                new NotificationProviderRequest(
                        notification.getId(),
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage()
                );

        NotificationProviderResponse response =
                notificationProvider.send(request);

        if (response.successful()) {

            persistenceService.markSent(
                    notification.getId(),
                    response
            );

            notificationMetrics.notificationSent();

        } else {

            persistenceService.markFailed(
                    notification.getId(),
                    response
            );

            notificationMetrics.notificationFailed();
        }
    }

//    /*
//     * Temporary recipient strategy.
//     *
//     * Later this should come from customer/profile data
//     * rather than constructing an address here.
//     */
//    private String buildRecipient(Long customerId) {
//
//        return "customer-" +
//                customerId +
//                "@example.com";
//    }
}