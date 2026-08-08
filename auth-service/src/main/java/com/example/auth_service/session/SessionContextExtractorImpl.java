package com.example.auth_service.session;

import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.security.device.BrowserDetector;
import com.example.auth_service.security.device.BrowserInfo;
import com.example.auth_service.security.device.DeviceDetector;
import com.example.auth_service.security.device.DeviceType;
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

    private final DeviceDetector deviceDetector;

    @Override
    public SessionInfo extract(SessionContext context) {

        HttpServletRequest request = context.getRequest();

        String userAgent = request.getHeader("User-Agent");

        BrowserInfo browser = browserDetector.detect(userAgent);

        OperatingSystemInfo operatingSystem = operatingSystemDetector.detect(userAgent);

        DeviceType deviceType = deviceDetector.detect(userAgent);

        String ipAddress = getClientIp(request);

        return SessionInfo.builder()
                .deviceName(
                        buildDeviceName(
                                deviceType,
                                operatingSystem
                        )
                )

                .deviceType(
                        deviceType.name()
                )

                .browser(
                        buildBrowserName(browser)
                )

                .operatingSystem(
                        buildOperatingSystemName(
                                operatingSystem
                        )
                )

                .ipAddress(ipAddress)
                .build();
    }

    private String buildDeviceName(DeviceType deviceType,
            OperatingSystemInfo operatingSystem) {

        if (deviceType == DeviceType.UNKNOWN) {
            return "Unknown Device";
        }

        return deviceType.name()
                .toLowerCase();
    }

    private String buildBrowserName(BrowserInfo browser) {

        if (browser.getBrowserVersion() == null
                || browser.getBrowserVersion().isBlank()) {

            return browser.getBrowserName();
        }

        return browser.getBrowserName()
                + " "
                + browser.getBrowserVersion();
    }

    private String buildOperatingSystemName(OperatingSystemInfo operatingSystem) {

        if (operatingSystem.getVersion() == null
                || operatingSystem.getVersion().isBlank()) {

            return operatingSystem.getOperatingSystem();
        }

        return operatingSystem.getOperatingSystem()
                + " "
                + operatingSystem.getVersion();
    }

    private String getClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null
                && !forwarded.isBlank()) {

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