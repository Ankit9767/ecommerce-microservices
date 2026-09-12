package com.example.reviews_service.client;

import com.ecommerce.common.dto.ReviewEligibilityResponse;
import com.ecommerce.common.security.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "order-service",
        configuration = FeignSecurityConfiguration.class
)
public interface OrderClient {

    @GetMapping("/api/orders/internal/{id}/review-eligibility")
    ReviewEligibilityResponse checkReviewEligibilityInternal(
            @PathVariable("id") Long id,
            @RequestParam("productId") Long productId,
            @RequestParam("userId") Long userId
    );
}