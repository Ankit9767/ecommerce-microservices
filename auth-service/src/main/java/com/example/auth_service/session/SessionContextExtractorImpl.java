package com.example.auth_service.session;

import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.security.device.BrowserDetector;
import com.example.auth_service.security.device.BrowserInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SessionContextExtractorImpl implements SessionContextExtractor {

    private final BrowserDetector browserDetector;

    @Override
    public SessionInfo extract(SessionContext context) {

        HttpServletRequest request = context.getRequest();

        String userAgent = request.getHeader("User-Agent");

        BrowserInfo browser = browserDetector.detect(userAgent);

        String ipAddress = getClientIp(request);

        return SessionInfo.builder()
                .browser(browser.getBrowserName() + " "
                        + browser.getBrowserVersion())
                .deviceName("Unknown")
                .operatingSystem("Unknown")
                .ipAddress(ipAddress)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }

}