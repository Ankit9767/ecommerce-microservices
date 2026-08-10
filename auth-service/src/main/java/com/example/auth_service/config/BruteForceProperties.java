package com.example.auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.brute-force")
public class BruteForceProperties {

    private int maxFailures = 5;

    private int maxIpFailures = 20;

    private Duration failureWindow = Duration.ofMinutes(15);

    private Duration lockDuration = Duration.ofMinutes(15);

    private Duration ipBlockDuration = Duration.ofMinutes(15);
}