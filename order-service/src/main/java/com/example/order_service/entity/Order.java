package com.example.order_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_customer", columnList = "customer_id"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_customer_status", columnList = "customer_id,status")
        }
)
public class Order extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    /*
     * Immutable shipping-address snapshot captured when the order is created.
     *
     * This intentionally lives on the Order rather than referencing a
     * customer address record, because the customer's saved address may
     * change after the order has been placed.
     */
    @Column(name = "shipping_recipient_name", nullable = false, length = 100)
    private String shippingRecipientName;

    @Column(name = "shipping_phone", nullable = false, length = 30)
    private String shippingPhone;

    @Column(name = "shipping_address_line1", nullable = false, length = 200)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2", length = 200)
    private String shippingAddressLine2;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false, length = 100)
    private String shippingState;

    @Column(name = "shipping_postal_code", nullable = false, length = 20)
    private String shippingPostalCode;

    @Column(name = "shipping_country", nullable = false, length = 2)
    private String shippingCountry;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "reservation_id", nullable = false, unique = true)
    private UUID reservationId;

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