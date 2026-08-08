package com.example.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller")
public class SellerTestController {

    @GetMapping("/test")
    public String sellerTest() {

        return "SELLER access granted";
    }
}