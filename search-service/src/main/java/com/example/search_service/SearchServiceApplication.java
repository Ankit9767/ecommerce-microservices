package com.example.search_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.search_service",
        "com.ecommerce.common"
})
@EnableJpaRepositories(basePackages = {
        "com.example.search_service",
        "com.ecommerce.common.kafka"
})
@EntityScan(basePackages = {
        "com.example.search_service",
        "com.ecommerce.common.kafka"
})
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}