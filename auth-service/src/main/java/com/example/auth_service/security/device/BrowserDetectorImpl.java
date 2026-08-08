package com.example.auth_service.security.device;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BrowserDetectorImpl implements BrowserDetector {

    @Override
    public BrowserInfo detect(String userAgent) {

        if (userAgent == null || userAgent.isBlank()) {

            return BrowserInfo.builder()
                    .browserName("Unknown")
                    .browserVersion("")
                    .build();
        }

        if (userAgent.contains("Edg/")) {
            return build(userAgent, "Edge", "Edg/");
        }

        if (userAgent.contains("OPR/")) {
            return build(userAgent, "Opera", "OPR/");
        }

        if (userAgent.contains("Chrome/")
                && !userAgent.contains("Edg/")
                && !userAgent.contains("OPR/")) {

            return build(userAgent, "Chrome", "Chrome/");
        }

        if (userAgent.contains("Firefox/")) {
            return build(userAgent, "Firefox", "Firefox/");
        }

        if (userAgent.contains("Safari/")
                && userAgent.contains("Version/")
                && !userAgent.contains("Chrome")) {

            return build(userAgent, "Safari", "Version/");
        }

        return BrowserInfo.builder()
                .browserName("Unknown")
                .browserVersion("")
                .build();
    }

    private BrowserInfo build(String userAgent,
                            String browser,
                            String prefix) {

        Pattern pattern = Pattern.compile(Pattern.quote(prefix) + "([\\d.]+)");
        Matcher matcher = pattern.matcher(userAgent);
        String version = "";

        if (matcher.find()) {
            version = matcher.group(1);
        }

        return BrowserInfo.builder()
                .browserName(browser)
                .browserVersion(version)
                .build();
    }
}