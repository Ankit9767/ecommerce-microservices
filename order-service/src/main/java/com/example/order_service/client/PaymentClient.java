//package com.example.order_service.client;
//
//import com.ecommerce.common.dto.PaymentRequest;
//import com.ecommerce.common.dto.PaymentResponse;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(name = "payment-service")
//public interface PaymentClient {
//
//    @PostMapping("/payments")
//    PaymentResponse processPayment(
//            @RequestBody PaymentRequest request);
//
//}
