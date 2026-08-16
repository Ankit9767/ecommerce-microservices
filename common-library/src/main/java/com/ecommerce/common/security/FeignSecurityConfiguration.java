package com.ecommerce.common.security;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignSecurityConfiguration {

    @Bean
    public RequestInterceptor feignSecurityInterceptor() {
        return new FeignSecurityInterceptor();
    }

    @Bean
    public ErrorDecoder commonFeignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}