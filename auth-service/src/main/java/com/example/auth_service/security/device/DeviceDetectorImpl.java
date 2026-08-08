package com.example.auth_service.security.device;

import org.springframework.stereotype.Component;

@Component
public class DeviceDetectorImpl implements DeviceDetector {

    @Override
    public DeviceType detect(String userAgent) {

        if (userAgent == null || userAgent.isBlank()) {
            return DeviceType.UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        /*
         * Tablet detection must happen before
         * generic mobile detection.
         */

        if (isTablet(ua)) {
            return DeviceType.TABLET;
        }

        if (isMobile(ua)) {
            return DeviceType.MOBILE;
        }

        /*
         * If the User-Agent is not mobile or tablet,
         * we treat it as a desktop device.
         */

        if (isDesktop(ua)) {
            return DeviceType.DESKTOP;
        }

        return DeviceType.UNKNOWN;
    }

    private boolean isTablet(String userAgent) {

        return userAgent.contains("ipad")
                || userAgent.contains("tablet")
                || userAgent.contains("playbook")
                || userAgent.contains("silk")
                || (userAgent.contains("android")
                        && !userAgent.contains("mobile")
        );
    }

    private boolean isMobile(String userAgent) {

        return userAgent.contains("mobile")
                || userAgent.contains("iphone")
                || userAgent.contains("ipod")
                || userAgent.contains("windows phone")
                || userAgent.contains("blackberry")
                || userAgent.contains("opera mini")
                || userAgent.contains("opera mobi");
    }

    private boolean isDesktop(String userAgent) {

        return userAgent.contains("windows")
                || userAgent.contains("macintosh")
                || userAgent.contains("linux")
                || userAgent.contains("x11")
                || userAgent.contains("cros");
    }
}