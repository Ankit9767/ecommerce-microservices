package com.example.auth_service.security.device;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OperatingSystemDetectorImpl
        implements OperatingSystemDetector {

    @Override
    public OperatingSystemInfo detect(String userAgent) {

        if (userAgent == null || userAgent.isBlank()) {
            return unknown();
        }

        // Windows
        if (userAgent.contains("Windows NT 10.0")) {

            return OperatingSystemInfo.builder()
                    .operatingSystem("Windows")
                    .version("10/11")
                    .build();
        }

        if (userAgent.contains("Windows NT 6.3")) {

            return OperatingSystemInfo.builder()
                    .operatingSystem("Windows")
                    .version("8.1")
                    .build();
        }

        if (userAgent.contains("Windows NT 6.2")) {

            return OperatingSystemInfo.builder()
                    .operatingSystem("Windows")
                    .version("8")
                    .build();
        }

        if (userAgent.contains("Windows NT 6.1")) {

            return OperatingSystemInfo.builder()
                    .operatingSystem("Windows")
                    .version("7")
                    .build();
        }

        // Android
        if (userAgent.contains("Android")) {

            String version = extractVersion(userAgent, "Android ");

            return OperatingSystemInfo.builder()
                    .operatingSystem("Android")
                    .version(version)
                    .build();
        }

        // iPhone / iPad
        if (userAgent.contains("iPhone")
                || userAgent.contains("iPad")
                || userAgent.contains("iPod")) {

            String version = extractVersion(userAgent, "OS ");

            version = version.replace("_", ".");

            return OperatingSystemInfo.builder()
                    .operatingSystem("iOS")
                    .version(version)
                    .build();
        }

        // macOS
        if (userAgent.contains("Mac OS X")) {

            String version = extractVersion(userAgent, "Mac OS X ");

            version = version.replace("_", ".");

            return OperatingSystemInfo.builder()
                    .operatingSystem("macOS")
                    .version(version)
                    .build();
        }

        // Linux
        if (userAgent.contains("Linux")) {

            return OperatingSystemInfo.builder()
                    .operatingSystem("Linux")
                    .version("")
                    .build();
        }

        return unknown();
    }

    private String extractVersion(String userAgent,
            String prefix) {

        Pattern pattern = Pattern.compile(Pattern.quote(prefix)
                                + "([0-9._]+)");

        Matcher matcher = pattern.matcher(userAgent);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private OperatingSystemInfo unknown() {

        return OperatingSystemInfo.builder()
                .operatingSystem("Unknown")
                .version("")
                .build();
    }
}