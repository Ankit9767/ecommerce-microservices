package com.example.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.notification_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.notification_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.notification_service",
        "com.ecommerce.common.kafka"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}