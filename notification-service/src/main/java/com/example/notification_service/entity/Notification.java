package com.example.notification_service.entity;

import com.example.notification_service.enums.NotificationStatus;
import com.example.notification_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notification_customer_id",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_notification_order_id",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_notification_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "notification_type",
            nullable = false,
            length = 50
    )
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private NotificationStatus status;

    @Column(
            name = "recipient",
            nullable = false,
            length = 255
    )
    private String recipient;

    @Column(
            name = "subject",
            nullable = false,
            length = 255
    )
    private String subject;

    @Column(
            name = "message",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    @Column(
            name = "provider_reference",
            length = 255
    )
    private String providerReference;

    @Column(
            name = "failure_reason",
            columnDefinition = "TEXT"
    )
    private String failureReason;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}