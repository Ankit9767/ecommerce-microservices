package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(
                        name = "idx_order_item_order",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_order_item_product",
                        columnList = "product_id"
                )
        }
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(
            name = "product_name",
            nullable = false,
            length = 150
    )
    private String productName;

    @Column(
            name = "sku",
            nullable = false,
            length = 100
    )
    private String sku;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(
            name = "line_total",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal lineTotal;
}