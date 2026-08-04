package com.example.auth_service.Service;

import com.example.auth_service.Dto.LoginRequest;
import com.example.auth_service.Dto.LoginResponse;
import com.example.auth_service.Dto.RegisterRequest;
import com.example.auth_service.Dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
