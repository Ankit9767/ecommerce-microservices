package com.example.notification_service.entity;

import com.example.notification_service.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "notification_provider_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_provider_notification_id",
                        columnNames = "notification_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_notification_provider_notification_id",
                        columnList = "notification_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationProviderTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "notification_id",
            nullable = false,
            unique = true
    )
    private Long notificationId;

    @Column(
            name = "provider_reference",
            nullable = false,
            unique = true
    )
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private NotificationStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;
}