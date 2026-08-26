package com.ecommerce.common.security;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignSecurityConfiguration {

    @Bean
    public RequestInterceptor feignSecurityInterceptor(
            @Value("${service.security.internal-token}")
            String internalServiceToken) {

        return new FeignSecurityInterceptor(internalServiceToken);
    }

    @Bean
    public ErrorDecoder commonFeignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}