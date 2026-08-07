//package com.example.auth_service.controller;
//
//import com.example.auth_service.service.AuthService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("/register")
//    public RegisterResponse register(
//            @Valid @RequestBody RegisterRequest request) {
//
//        return authService.register(request);
//    }
//
//    @PostMapping("/login")
//    public LoginResponse login(
//            @Valid @RequestBody LoginRequest request) {
//
//        return authService.login(request);
//    }
//}
