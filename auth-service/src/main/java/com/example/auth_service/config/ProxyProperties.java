package com.example.auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.proxy")
public class ProxyProperties {

    private boolean enabled = false;

    private List<String> trustedProxies = new ArrayList<>();
}