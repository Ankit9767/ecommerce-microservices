package com.example.product_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.product_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.product_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.product_service",
        "com.ecommerce.common.kafka"
})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
