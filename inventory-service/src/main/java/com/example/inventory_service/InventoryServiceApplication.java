package com.example.inventory_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.inventory_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.inventory_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.inventory_service",
        "com.ecommerce.common.kafka"
})
@EnableFeignClients
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

}
