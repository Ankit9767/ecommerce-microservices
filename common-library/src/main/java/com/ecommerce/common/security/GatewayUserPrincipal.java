package com.ecommerce.common.security;

public record GatewayUserPrincipal(Long userId,
                                   String username) {
}