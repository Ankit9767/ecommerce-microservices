package com.example.payment_service.controller;

import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.dto.CreatePaymentRequest;
import com.example.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                         Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        paymentService.createPayment(
                                request,
                                authentication
                        )
                );
    }

    @GetMapping
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(Pageable pageable) {

        return ResponseEntity.ok(
                paymentService.getAllPayments(pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id,
                                                      Authentication authentication) {

        return ResponseEntity.ok(
                paymentService.getPayment(
                        id,
                        authentication
                )
        );
    }

    @GetMapping("/my")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(Authentication authentication,
                                                               Pageable pageable) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(
                        authentication,
                        pageable
                )
        );
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status,
                                                                     Pageable pageable) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(
                        status,
                        pageable
                )
        );
    }
}