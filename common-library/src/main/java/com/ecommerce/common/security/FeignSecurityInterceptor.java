package com.ecommerce.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignSecurityInterceptor implements RequestInterceptor {

    private final String internalServiceToken;

    public FeignSecurityInterceptor(String internalServiceToken) {
        this.internalServiceToken = internalServiceToken;
    }

    @Override
    public void apply(RequestTemplate template) {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();

        if (attributes != null) {

            HttpServletRequest request = attributes.getRequest();

            copyHeader(
                    request,
                    template,
                    GatewaySecurityHeaders.AUTHENTICATED_USER
            );

            copyHeader(
                    request,
                    template,
                    GatewaySecurityHeaders.AUTHENTICATED_USER_ID
            );

            copyHeader(
                    request,
                    template,
                    GatewaySecurityHeaders.AUTHENTICATED_USER_EMAIL
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

            return;
        }

        if (internalServiceToken == null || internalServiceToken.isBlank()) {

            throw new IllegalStateException(
                    "Internal service token is not configured"
            );
        }

        template.header(
                "X-Service-Token",
                internalServiceToken
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