package com.example.auth_service.security.jwt;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JwtClaims {

    private String username;

    private List<String> roles;

    private JwtTokenType tokenType;

}
