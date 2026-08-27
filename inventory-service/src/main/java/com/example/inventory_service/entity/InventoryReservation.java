package com.example.inventory_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "inventory_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_reservation_product",
                        columnNames = {"reservation_id", "product_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_inventory_reservation_id",
                        columnList = "reservation_id"
                ),
                @Index(
                        name = "idx_inventory_reservation_product",
                        columnList = "reservation_id,product_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservation extends BaseEntity {

    @Column(
            name = "reservation_id",
            nullable = false
    )
    private UUID reservationId;

    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private ReservationStatus status;
}