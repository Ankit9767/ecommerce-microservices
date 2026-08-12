package com.ecommerce.common.security;

public final class GatewaySecurityHeaders {

    private GatewaySecurityHeaders() {}

    public static final String AUTHENTICATED_USER = "X-Authenticated-User";

    public static final String USER_ROLES = "X-User-Roles";
}