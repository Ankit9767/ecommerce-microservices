package com.example.inventory_service.client;

import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.security.FeignSecurityConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        configuration = FeignSecurityConfiguration.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProduct(
            @PathVariable("id") Long productId
    );
}