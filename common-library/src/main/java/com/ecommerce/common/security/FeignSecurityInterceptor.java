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

        /*
         * Every Feign call from one backend service to another
         * must carry the internal service token.
         */
        if (internalServiceToken == null || internalServiceToken.isBlank()) {

            throw new IllegalStateException(
                    "Internal service token is not configured"
            );
        }

        template.header("X-Service-Token", internalServiceToken);

        /*
         * If this Feign call originated from an HTTP request,
         * optionally propagate the original user's identity.
         */
        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return;
        }

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