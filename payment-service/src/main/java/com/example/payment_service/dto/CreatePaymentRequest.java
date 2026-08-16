package com.example.payment_service.dto;

import com.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreatePaymentRequest(

        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotBlank(message = "Currency is required")
        @Pattern(
                regexp = "^[A-Z]{3}$",
                message = "Currency must be a valid 3-letter ISO code"
        )
        String currency,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}