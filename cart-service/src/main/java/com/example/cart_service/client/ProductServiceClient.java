package com.example.cart_service.client;

import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.exception.RemoteServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceClient {

    private final ProductClient productClient;

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "getProductFallback"
    )
    public ProductResponse getProduct(Long productId) {

        return productClient.getProduct(productId);
    }

    private ProductResponse getProductFallback(Long productId,
                                               Throwable throwable) {

        throw new RemoteServiceUnavailableException(
                "product-service"
        );
    }
}