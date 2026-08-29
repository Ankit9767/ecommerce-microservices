package com.ecommerce.common.security;

public final class GatewaySecurityHeaders {

    private GatewaySecurityHeaders() {}

    public static final String AUTHENTICATED_USER = "X-Authenticated-User";

    public static final String AUTHENTICATED_USER_ID = "X-Authenticated-User-Id";

    public static final String AUTHENTICATED_USER_EMAIL = "X-Authenticated-User-Email";

    public static final String USER_ROLES = "X-User-Roles";
}