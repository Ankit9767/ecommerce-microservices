package com.example.order_service.service;

import com.ecommerce.common.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusLifecycle {

    private final Map<OrderStatus, Set<OrderStatus>> allowedTransitions =
            new EnumMap<>(OrderStatus.class);

    public OrderStatusLifecycle() {

        allowedTransitions.put(
                OrderStatus.PENDING_PAYMENT,
                EnumSet.of(
                        OrderStatus.PAID,
                        OrderStatus.CANCELLED
                )
        );

        allowedTransitions.put(
                OrderStatus.PAID,
                EnumSet.of(
                        OrderStatus.PROCESSING,
                        OrderStatus.CANCELLED
                )
        );

        allowedTransitions.put(
                OrderStatus.PROCESSING,
                EnumSet.of(
                        OrderStatus.SHIPPED
                )
        );

        allowedTransitions.put(
                OrderStatus.SHIPPED,
                EnumSet.of(
                        OrderStatus.DELIVERED
                )
        );

        allowedTransitions.put(
                OrderStatus.DELIVERED,
                EnumSet.noneOf(OrderStatus.class)
        );

        allowedTransitions.put(
                OrderStatus.CANCELLED,
                EnumSet.noneOf(OrderStatus.class)
        );
    }

    public boolean canTransition(OrderStatus currentStatus,
                                 OrderStatus targetStatus) {

        if (currentStatus == null || targetStatus == null) {
            return false;
        }

        Set<OrderStatus> allowed = allowedTransitions.get(currentStatus);

        return allowed != null && allowed.contains(targetStatus);
    }
}