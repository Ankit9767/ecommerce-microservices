package com.example.cart_service.service;

import com.ecommerce.common.dto.CartResponse;
import com.example.cart_service.dto.AddCartItemRequest;
import com.example.cart_service.dto.UpdateCartItemRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface CartService {

    CartResponse getCart(Authentication authentication);

    CartResponse addItem(AddCartItemRequest request, Authentication authentication);

    CartResponse updateItem(Long productId, UpdateCartItemRequest request,
                            Authentication authentication);

    CartResponse removeItem(Long productId, Authentication authentication);

    CartResponse clearCart(Authentication authentication);

    Page<CartResponse> getAllCarts(Pageable pageable);
}