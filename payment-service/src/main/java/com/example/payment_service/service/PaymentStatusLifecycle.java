package com.example.payment_service.service;

import com.ecommerce.common.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentStatusLifecycle {

    private final Map<PaymentStatus, Set<PaymentStatus>> transitions =
            new EnumMap<>(PaymentStatus.class);

    public PaymentStatusLifecycle() {

        transitions.put(
                PaymentStatus.PENDING,
                EnumSet.of(
                        PaymentStatus.PROCESSING,
                        PaymentStatus.SUCCESS,
                        PaymentStatus.FAILED,
                        PaymentStatus.CANCELLED
                )
        );

        transitions.put(
                PaymentStatus.PROCESSING,
                EnumSet.of(
                        PaymentStatus.SUCCESS,
                        PaymentStatus.FAILED,
                        PaymentStatus.CANCELLED
                )
        );

        transitions.put(
                PaymentStatus.SUCCESS,
                EnumSet.of(
                        PaymentStatus.REFUNDED
                )
        );

        transitions.put(
                PaymentStatus.FAILED,
                EnumSet.of(
                        PaymentStatus.PENDING
                )
        );

        transitions.put(
                PaymentStatus.CANCELLED,
                EnumSet.noneOf(PaymentStatus.class)
        );

        transitions.put(
                PaymentStatus.REFUNDED,
                EnumSet.noneOf(PaymentStatus.class)
        );
    }

    public boolean canTransition(PaymentStatus currentStatus,
                                 PaymentStatus targetStatus) {

        if (currentStatus == null || targetStatus == null) {
            return false;
        }

        if (currentStatus == targetStatus) {
            return false;
        }

        return transitions
                .getOrDefault(
                        currentStatus,
                        EnumSet.noneOf(PaymentStatus.class)
                )
                .contains(targetStatus);
    }
}