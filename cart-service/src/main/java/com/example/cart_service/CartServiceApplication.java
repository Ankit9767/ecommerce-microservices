package com.example.cart_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.cart_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.cart_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.cart_service",
        "com.ecommerce.common.kafka"
})
@EnableFeignClients
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
