package com.example.order_service.client;

import com.ecommerce.common.dto.InventoryQuantityRequest;
import com.ecommerce.common.dto.InventoryResponse;
import com.ecommerce.common.security.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "inventory-service",
        configuration = FeignSecurityConfiguration.class
)
public interface InventoryClient {

    @PostMapping("/api/inventory/{productId}/reserve")
    InventoryResponse reserveStock(
            @PathVariable("productId") Long productId,
            @RequestBody InventoryQuantityRequest request
    );

    @PostMapping("/api/inventory/{productId}/release")
    InventoryResponse releaseStock(
            @PathVariable("productId") Long productId,
            @RequestBody InventoryQuantityRequest request
    );

    @PostMapping("/api/inventory/{productId}/confirm")
    InventoryResponse confirmReservation(
            @PathVariable("productId") Long productId,
            @RequestBody InventoryQuantityRequest request
    );
}
