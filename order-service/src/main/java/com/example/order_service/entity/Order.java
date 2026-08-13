package com.example.order_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.ecommerce.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(
                        name = "idx_order_customer",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_order_status",
                        columnList = "status"
                )
        }
)
public class Order extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Builder.Default
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    @Override
    public void onCreate() {

        super.onCreate();

        if (status == null) {
            status = OrderStatus.PENDING_PAYMENT;
        }
    }

    public void addItem(OrderItem item) {

        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {

        items.remove(item);
        item.setOrder(null);
    }
}