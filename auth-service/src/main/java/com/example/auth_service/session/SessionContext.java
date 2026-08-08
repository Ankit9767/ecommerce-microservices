package com.example.auth_service.session;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionContext {

    private HttpServletRequest request;

}