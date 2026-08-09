package com.example.auth_service.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PermissionTestController {

    @GetMapping("/products")
    public String productRead() {

        return "PRODUCT_READ permission granted";
    }

    @PostMapping("/products")
    public String productCreate() {

        return "PRODUCT_CREATE permission granted";
    }

    @GetMapping("/orders")
    public String orderRead() {

        return "ORDER_READ permission granted";
    }

    @PostMapping("/orders")
    public String orderCreate() {

        return "ORDER_CREATE permission granted";
    }
}