package com.example.shipping_service.entity;

import com.example.shipping_service.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "shipments",
        indexes = {
                @Index(
                        name = "idx_shipment_order_id",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_shipment_customer_id",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_shipment_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_shipment_tracking_number",
                        columnList = "tracking_number"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shipment_order_id",
                        columnNames = "order_id"
                ),
                @UniqueConstraint(
                        name = "uk_shipment_tracking_number",
                        columnNames = "tracking_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(
            name = "tracking_number",
            unique = true,
            length = 100
    )
    private String trackingNumber;

    @Column(
            name = "carrier",
            length = 100
    )
    private String carrier;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (status == null) {
            status = ShipmentStatus.CREATED;
        }
    }
}