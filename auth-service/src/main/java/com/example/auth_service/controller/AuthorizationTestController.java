package com.example.auth_service.controller;

import com.example.auth_service.service.AuthorizationTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security-test")
@RequiredArgsConstructor
public class AuthorizationTestController {

    private final AuthorizationTestService authorizationTestService;


    @GetMapping("/customer")
    public String customer() {

        return authorizationTestService
                .customerOperation();
    }


    @GetMapping("/product-read")
    public String productRead() {

        return authorizationTestService
                .productReadOperation();
    }


    @GetMapping("/product-create")
    public String productCreate() {

        return authorizationTestService
                .productCreateOperation();
    }


    @GetMapping("/admin")
    public String admin() {

        return authorizationTestService
                .adminOperation();
    }


    @GetMapping("/seller")
    public String seller() {

        return authorizationTestService
                .sellerOperation();
    }
}