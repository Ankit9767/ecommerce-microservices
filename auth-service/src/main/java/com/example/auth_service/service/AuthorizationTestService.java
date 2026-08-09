package com.example.auth_service.service;

public interface AuthorizationTestService {

    String customerOperation();

    String productReadOperation();

    String productCreateOperation();

    String adminOperation();

    String sellerOperation();
}