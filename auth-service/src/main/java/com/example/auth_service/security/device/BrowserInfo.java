package com.example.auth_service.security.device;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrowserInfo {

    private String browserName;

    private String browserVersion;

}