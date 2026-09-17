package com.example.payment_service.config;

import com.razorpay.RazorpayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RazorpayProperties.class)
public class RazorpayConfig {

    @Bean
    @ConditionalOnProperty(
            name = "payment.provider",
            havingValue = "RAZORPAY"
    )
    public RazorpayClient razorpayClient(RazorpayProperties properties) {

        if (properties.getKeyId() == null
                || properties.getKeyId().isBlank()) {
            throw new IllegalStateException(
                    "RAZORPAY_KEY_ID is required when PAYMENT_PROVIDER=RAZORPAY"
            );
        }

        if (properties.getKeySecret() == null
                || properties.getKeySecret().isBlank()) {
            throw new IllegalStateException(
                    "RAZORPAY_KEY_SECRET is required when PAYMENT_PROVIDER=RAZORPAY"
            );
        }

        try {
            return new RazorpayClient(
                    properties.getKeyId(),
                    properties.getKeySecret()
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to initialize Razorpay client",
                    e
            );
        }
    }
}