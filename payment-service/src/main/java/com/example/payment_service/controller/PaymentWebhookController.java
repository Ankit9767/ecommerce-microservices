package com.example.payment_service.controller;

import com.example.payment_service.config.RazorpayProperties;
import com.example.payment_service.service.PaymentWebhookService;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentWebhookService webhookService;

    private final RazorpayProperties razorpayProperties;

    @PostMapping("/webhook/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false)
            String signature,
            @RequestHeader(value = "x-razorpay-event-id", required = false)
            String eventId,
            @RequestBody String rawBody) {

        log.info(
                "========== RAZORPAY WEBHOOK HIT ==========" +
                        " eventId={}, signaturePresent={}, bodyLength={}",
                eventId,
                signature != null && !signature.isBlank(),
                rawBody != null ? rawBody.length() : 0
        );

        if (signature == null || signature.isBlank()) {

            log.warn("Razorpay webhook rejected because signature is missing");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        if (razorpayProperties.getWebhookSecret() == null ||
                razorpayProperties.getWebhookSecret().isBlank()) {

            log.error(
                    "Razorpay webhook secret is not configured"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        try {

            boolean validSignature =
                    Utils.verifyWebhookSignature(
                            rawBody,
                            signature,
                            razorpayProperties.getWebhookSecret()
                    );

            if (!validSignature) {

                log.warn(
                        "Invalid Razorpay webhook signature. eventId={}",
                        eventId
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .build();
            }

        } catch (RazorpayException ex) {

            log.error(
                    "Razorpay webhook signature verification failed. eventId={}",
                    eventId,
                    ex
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        webhookService.processRazorpayWebhook(
                rawBody,
                eventId
        );

        /*
         * Razorpay expects a successful 2xx response for an accepted
         * webhook. Returning 200 prevents unnecessary webhook retries.
         */
        return ResponseEntity.ok().build();
    }
}