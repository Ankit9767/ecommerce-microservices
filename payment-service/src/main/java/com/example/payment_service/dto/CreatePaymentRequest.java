package com.example.payment_service.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "Order ID is required")
        Long orderId
) {
}