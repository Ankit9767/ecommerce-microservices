package com.ecommerce.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignSecurityInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignSecurityInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();

        if (attributes == null) {

            log.error("NO ServletRequestAttributes available!");

            return;
        }

        HttpServletRequest request = attributes.getRequest();

        String username =
                request.getHeader(
                        GatewaySecurityHeaders.AUTHENTICATED_USER
                );

        String roles =
                request.getHeader(
                        GatewaySecurityHeaders.USER_ROLES
                );

        String authorization = request.getHeader("Authorization");

        copyHeader(
                request,
                template,
                GatewaySecurityHeaders.AUTHENTICATED_USER
        );

        copyHeader(
                request,
                template,
                GatewaySecurityHeaders.USER_ROLES
        );

        copyHeader(
                request,
                template,
                "Authorization"
        );

        copyHeader(
                request,
                template,
                "X-Request-ID"
        );
    }

    private void copyHeader(
            HttpServletRequest request,
            RequestTemplate template,
            String headerName) {

        String value = request.getHeader(headerName);

        if (value != null && !value.isBlank()) {

            template.header(headerName, value);
        }
    }
}