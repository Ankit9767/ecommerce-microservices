package com.example.payment_service.entity;

import com.ecommerce.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "payment_provider_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_transaction_payment_id",
                        columnNames = "payment_id"
                ),
                @UniqueConstraint(
                        name = "uk_provider_transaction_reference",
                        columnNames = "provider_reference"
                )
        },
        indexes = {
                @Index(
                        name = "idx_provider_transaction_payment_id",
                        columnList = "payment_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProviderTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "payment_id",
            nullable = false,
            unique = true
    )
    private Long paymentId;

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
    private PaymentStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}