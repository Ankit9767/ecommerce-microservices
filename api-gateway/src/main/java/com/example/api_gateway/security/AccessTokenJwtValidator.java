package com.example.api_gateway.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenJwtValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        String tokenType = jwt.getClaimAsString("TOKEN_TYPE");

        if (!"ACCESS".equals(tokenType)) {

            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Token is not an access token",
                    null
            );

            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}