package com.example.order_service.client;

import com.ecommerce.common.dto.CartResponse;
import com.ecommerce.common.security.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "cart-service",
        configuration = FeignSecurityConfiguration.class
)
public interface CartClient {

    @GetMapping("/api/cart")
    CartResponse getCart();

    @DeleteMapping("/api/cart")
    void clearCart();
}
