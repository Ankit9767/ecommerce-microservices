//package com.example.order_service.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestClient;
//
//@Configuration
//public class RestClientConfig {
//
//    @Value("${service.product-service.url}")
//    private String productServiceUrl;
//
//    @Bean
//    RestClient productRestClient(RestClient.Builder builder) {
//        return builder
//                .baseUrl(productServiceUrl)
//                .build();
//    }
//}

//was needed when using rest client , now using openFeign