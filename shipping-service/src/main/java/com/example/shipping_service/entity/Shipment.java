package com.example.shipping_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.example.shipping_service.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
public class Shipment extends BaseEntity {

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

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    @Builder.Default
    private Long version = 0L;

    @PrePersist
    public void initialize() {

        if (status == null) {
            status = ShipmentStatus.CREATED;
        }

        if (version == null) {
            version = 0L;
        }
    }
}