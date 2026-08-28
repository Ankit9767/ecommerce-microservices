package com.example.notification_service.service.impl;

import com.ecommerce.common.events.OrderCancelledEvent;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.PaymentCompletedEvent;
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

    public void handleOrderCreated(OrderCreatedEvent event) {

        Notification notification =
                persistenceService.createPending(
                        Notification.builder()
                                .customerId(event.getCustomerId())
                                .orderId(event.getOrderId())
                                .type(NotificationType.ORDER_CREATED)
                                .recipient(
                                        buildRecipient(
                                                event.getCustomerId()
                                        )
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
                                        buildRecipient(
                                                event.getCustomerId()
                                        )
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
                                        buildRecipient(
                                                event.getCustomerId()
                                        )
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
                                        buildRecipient(
                                                event.getCustomerId()
                                        )
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

    /*
     * Temporary recipient strategy.
     *
     * Later this should come from customer/profile data
     * rather than constructing an address here.
     */
    private String buildRecipient(Long customerId) {

        return "customer-" +
                customerId +
                "@example.com";
    }
}