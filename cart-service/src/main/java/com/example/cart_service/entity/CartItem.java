package com.example.cart_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "cart_items",
        indexes = {
                @Index(
                        name = "idx_cart_item_cart_id",
                        columnList = "cart_id"
                ),
                @Index(
                        name = "idx_cart_item_product_id",
                        columnList = "product_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_product",
                        columnNames = {"cart_id", "product_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cart_id",
            nullable = false
    )
    private Cart cart;

    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;

    @Column(
            nullable = false
    )
    private Integer quantity;

    /*
     * Snapshot of the product price when it was added.
     *
     * Final pricing will still be obtained from Product Service
     * during checkout/order creation.
     */
    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            name = "product_name",
            length = 255
    )
    private String productName;

    @Column(
            name = "sku",
            length = 100
    )
    private String sku;
}