//package com.example.order_service.client;
//
//
//import com.ecommerce.common.dto.ProductResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestClient;
//
//@Component
//@RequiredArgsConstructor
//public class ProductClientImpl implements ProductClient {
//
//    private final RestClient productRestClient;
//
//    @Override
//    public ProductResponse getProduct(Long productId) {
//
//        return productRestClient.get()
//                .uri("/api/products/{id}", productId)
//                .retrieve()
//                .body(ProductResponse.class);
//    }
//}

//was needed when using rest client , now using openFeign