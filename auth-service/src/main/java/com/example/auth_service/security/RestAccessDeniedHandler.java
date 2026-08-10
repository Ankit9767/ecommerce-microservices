package com.example.auth_service.security;

import com.example.auth_service.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        ApiError error =
                ApiError.builder()
                        .timestamp(LocalDateTime.from(Instant.now()))
                        .status(403)
                        .error("Forbidden")
                        .message("Access denied")
                        .path(request.getRequestURI())
                        .build();

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}