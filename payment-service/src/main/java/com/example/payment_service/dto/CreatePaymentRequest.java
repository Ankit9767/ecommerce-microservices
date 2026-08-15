package com.example.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotBlank(message = "Payment method is required")
        String paymentMethod
) {
}