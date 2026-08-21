package com.example.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = {
        "com.example.payment_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.payment_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.payment_service",
        "com.ecommerce.common.kafka"
})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
