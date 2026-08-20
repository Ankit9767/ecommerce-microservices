package com.ecommerce.common.kafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;

@Configuration
@Profile("!test")
@EnableKafka
@EnableKafkaRetryTopic
public class CommonKafkaRetryConfig {
}