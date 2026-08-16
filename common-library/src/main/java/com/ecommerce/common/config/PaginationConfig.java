package com.ecommerce.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
@EnableConfigurationProperties(PaginationProperties.class)
public class PaginationConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer(
            PaginationProperties properties) {

        return resolver -> {

            resolver.setMaxPageSize(
                    properties.maxPageSize()
            );

            resolver.setFallbackPageable(
                    PageRequest.of(
                            0,
                            properties.defaultPageSize()
                    )
            );

            resolver.setOneIndexedParameters(false);
        };
    }
}