package com.example.auth_service.service;

import com.example.auth_service.dto.request.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

}
