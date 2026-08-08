package com.example.auth_service.session;

import com.example.auth_service.dto.session.SessionInfo;

public interface SessionContextExtractor {

    SessionInfo extract(SessionContext context);

}