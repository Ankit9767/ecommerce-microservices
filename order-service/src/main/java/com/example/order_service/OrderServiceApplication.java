package com.example.order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.example.order_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.order_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.order_service",
        "com.ecommerce.common.kafka"
})
@EnableFeignClients
@EnableScheduling
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
