package com.example.payment_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.ecommerce.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_customer_id", columnList = "customer_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_provider_reference",
                        columnList = "provider_reference")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private Long orderId;

    @Column(
            name = "customer_id",
            nullable = false
    )
    private Long customerId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(
            name = "payment_method",
            length = 50
    )
    private String paymentMethod;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "provider_reference",
            unique = true,
            length = 255
    )
    private String providerReference;

    @Column(
            name = "failure_reason",
            length = 500
    )
    private String failureReason;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}