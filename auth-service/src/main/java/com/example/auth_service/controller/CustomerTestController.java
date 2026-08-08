package com.example.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
public class CustomerTestController {

    @GetMapping("/test")
    public String customerTest() {

        return "CUSTOMER access granted";
    }
}