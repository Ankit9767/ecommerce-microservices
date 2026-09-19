package com.example.payment_service.entity;

import com.ecommerce.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_provider_transactions",
        indexes = {
                @Index(
                        name = "idx_provider_transaction_payment_id",
                        columnList = "payment_id"
                ),
                @Index(
                        name = "idx_provider_transaction_order_id",
                        columnList = "provider_order_id"
                ),
                @Index(
                        name = "idx_provider_transaction_payment_ref",
                        columnList = "provider_payment_id"
                ),
                @Index(
                        name = "idx_provider_transaction_reference",
                        columnList = "provider_reference"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_transaction_payment_id",
                        columnNames = "payment_id"
                ),
                @UniqueConstraint(
                        name = "uk_provider_transaction_order_id",
                        columnNames = "provider_order_id"
                ),
                @UniqueConstraint(
                        name = "uk_provider_transaction_payment_ref",
                        columnNames = "provider_payment_id"
                ),
                @UniqueConstraint(
                        name = "uk_provider_transaction_reference",
                        columnNames = "provider_reference"
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
            name = "provider_order_id",
            nullable = false,
            unique = true,
            length = 255
    )
    private String providerOrderId;

    @Column(
            name = "provider_payment_id",
            unique = true,
            length = 255
    )
    private String providerPaymentId;

    @Column(
            name = "provider_reference",
            nullable = false,
            unique = true,
            length = 255
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
            nullable = false
    )
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}