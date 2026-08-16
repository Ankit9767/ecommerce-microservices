package com.ecommerce.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pagination")
public record PaginationProperties(
        int defaultPageSize,
        int maxPageSize
) {
}