package com.example.cart_service.controller;

import com.ecommerce.common.dto.CartResponse;
import com.example.cart_service.dto.AddCartItemRequest;
import com.example.cart_service.dto.UpdateCartItemRequest;
import com.example.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasAuthority('CART_READ')")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(authentication)
        );
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('CART_CREATE')")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request,
                                                Authentication authentication) {

        return ResponseEntity.ok(
                cartService.addItem(
                        request,
                        authentication
                )
        );
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasAuthority('CART_UPDATE')")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long productId,
                                                   @Valid @RequestBody UpdateCartItemRequest request,
                                                   Authentication authentication) {

        return ResponseEntity.ok(
                cartService.updateItem(
                        productId,
                        request,
                        authentication
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasAuthority('CART_DELETE')")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId,
                                                   Authentication authentication) {

        return ResponseEntity.ok(
                cartService.removeItem(
                        productId,
                        authentication
                )
        );
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('CART_DELETE')")
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {

        return ResponseEntity.ok(
                cartService.clearCart(authentication)
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('CART_READ_ALL')")
    public ResponseEntity<Page<CartResponse>> getAllCarts(Pageable pageable) {

        return ResponseEntity.ok(
                cartService.getAllCarts(pageable)
        );
    }

    // ============================================================
    // INTERNAL SERVICE APIs
    // ============================================================

    @GetMapping("/internal")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL_SERVICE')")
    public ResponseEntity<CartResponse> getCartInternal(@RequestParam Long customerId) {

        return ResponseEntity.ok(
                cartService.getCartByCustomerId(customerId)
        );
    }

    @DeleteMapping("/internal")
    @PreAuthorize("hasAuthority('ROLE_INTERNAL_SERVICE')")
    public ResponseEntity<Void> clearCartInternal(@RequestParam Long customerId) {

        cartService.clearCartByCustomerId(customerId);

        return ResponseEntity.noContent().build();
    }
}