package com.example.auth_service.session;

import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.security.device.BrowserDetector;
import com.example.auth_service.security.device.BrowserInfo;
import com.example.auth_service.security.device.OperatingSystemDetector;
import com.example.auth_service.security.device.OperatingSystemInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionContextExtractorImpl implements SessionContextExtractor {

    private final BrowserDetector browserDetector;

    private final OperatingSystemDetector operatingSystemDetector;

    @Override
    public SessionInfo extract(SessionContext context) {

        HttpServletRequest request = context.getRequest();

        String userAgent = request.getHeader("User-Agent");

        BrowserInfo browser = browserDetector.detect(userAgent);

        OperatingSystemInfo operatingSystem = operatingSystemDetector.detect(userAgent);

        String ipAddress = getClientIp(request);

        return SessionInfo.builder()
                .browser(
                        browser.getBrowserName()
                                + (browser.getBrowserVersion()
                                        .isBlank()
                                        ? ""
                                        : " " +
                                        browser.getBrowserVersion()
                        )
                )

                .operatingSystem(
                        operatingSystem.getOperatingSystem()
                                + (operatingSystem.getVersion()
                                        .isBlank()
                                        ? ""
                                        : " " +
                                        operatingSystem.getVersion()
                        )
                )

                .deviceName("Unknown")
                .ipAddress(ipAddress)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded
                    .split(",")[0]
                    .trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

}