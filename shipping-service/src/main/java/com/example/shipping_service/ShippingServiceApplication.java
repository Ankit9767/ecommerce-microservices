package com.example.shipping_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.example.shipping_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.shipping_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.shipping_service",
        "com.ecommerce.common.kafka"
})
@EnableFeignClients
@EnableScheduling
public class ShippingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShippingServiceApplication.class, args);
    }


}