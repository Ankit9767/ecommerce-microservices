package com.ecommerce.common.dto;

public record ReviewEligibilityResponse(
        boolean eligible,
        String reason
) {
}