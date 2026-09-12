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
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
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
    @PreAuthorize("hasAuthority('PAYMENT_READ_ALL')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(Pageable pageable) {

        return ResponseEntity.ok(
                paymentService.getAllPayments(pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
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
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
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
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
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