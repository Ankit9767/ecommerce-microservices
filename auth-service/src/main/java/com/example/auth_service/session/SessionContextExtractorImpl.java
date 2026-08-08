package com.example.auth_service.session;

import com.example.auth_service.dto.session.SessionInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class SessionContextExtractorImpl implements SessionContextExtractor {

    @Override
    public SessionInfo extract(SessionContext context) {

        HttpServletRequest request = context.getRequest();

        String userAgent = request.getHeader("User-Agent");

        String ipAddress = getClientIp(request);

        return SessionInfo.builder()
                .browser(userAgent)
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