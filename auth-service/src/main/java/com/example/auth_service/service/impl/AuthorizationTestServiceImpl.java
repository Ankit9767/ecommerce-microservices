package com.example.auth_service.service.impl;

import com.example.auth_service.service.AuthorizationTestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationTestServiceImpl
        implements AuthorizationTestService {

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customerOperation() {

        return "CUSTOMER method access granted";
    }


    @Override
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public String productReadOperation() {

        return "PRODUCT_READ method access granted";
    }


    @Override
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public String productCreateOperation() {

        return "PRODUCT_CREATE method access granted";
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOperation() {

        return "ADMIN method access granted";
    }


    @Override
    @PreAuthorize("hasRole('SELLER')")
    public String sellerOperation() {

        return "SELLER method access granted";
    }
}