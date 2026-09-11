package com.example.order_service.client;

import com.ecommerce.common.dto.CartResponse;
import com.ecommerce.common.security.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "cart-service",
        configuration = FeignSecurityConfiguration.class
)
public interface CartClient {

    @GetMapping("/api/cart/internal")
    CartResponse getCartInternal(@RequestParam("customerId") Long customerId);

    @DeleteMapping("/api/cart/internal")
    void clearCartInternal(@RequestParam("customerId") Long customerId);
}
