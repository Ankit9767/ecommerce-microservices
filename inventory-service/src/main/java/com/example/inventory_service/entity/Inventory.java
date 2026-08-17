package com.example.inventory_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "inventories",
        indexes = {
                @Index(
                        name = "idx_inventory_product_id",
                        columnList = "product_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_product",
                        columnNames = "product_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @Column(
            name = "product_id",
            nullable = false,
            unique = true
    )
    private Long productId;

    @Column(
            nullable = false
    )
    @Builder.Default
    private Integer quantity = 0;

    @Column(
            name = "reserved_quantity",
            nullable = false
    )
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Version
    @Column(nullable = false)
    private Long version;

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public void increaseStock(int amount) {
        validatePositiveAmount(amount);

        this.quantity += amount;
    }

    public void decreaseStock(int amount) {
        validatePositiveAmount(amount);

        if (amount > getAvailableQuantity()) {
            throw new IllegalStateException(
                    "Insufficient available inventory for product: "
                            + productId
            );
        }

        this.quantity -= amount;
    }

    public void reserveStock(int amount) {
        validatePositiveAmount(amount);

        if (amount > getAvailableQuantity()) {
            throw new IllegalStateException(
                    "Insufficient available inventory for product: "
                            + productId
            );
        }

        this.reservedQuantity += amount;
    }

    public void releaseStock(int amount) {
        validatePositiveAmount(amount);

        if (amount > reservedQuantity) {
            throw new IllegalStateException(
                    "Cannot release more stock than currently reserved"
            );
        }

        this.reservedQuantity -= amount;
    }

    public void confirmReservation(int amount) {
        validatePositiveAmount(amount);

        if (amount > reservedQuantity) {
            throw new IllegalStateException(
                    "Cannot confirm more stock than currently reserved"
            );
        }

        this.reservedQuantity -= amount;
        this.quantity -= amount;
    }

    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Inventory quantity must be greater than zero"
            );
        }
    }
}