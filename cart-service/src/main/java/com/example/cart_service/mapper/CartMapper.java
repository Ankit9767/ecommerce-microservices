package com.example.cart_service.mapper;

import com.ecommerce.common.dto.CartItemResponse;
import com.ecommerce.common.dto.CartResponse;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {

        List<CartItemResponse> items =
                cart.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        BigDecimal totalAmount =
                items.stream()
                        .map(CartItemResponse::lineTotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        Integer totalItems =
                items.stream()
                        .mapToInt(CartItemResponse::quantity)
                        .sum();

        return new CartResponse(
                cart.getId(),
                cart.getCustomerId(),
                items,
                totalAmount,
                totalItems
        );
    }

    private CartItemResponse toItemResponse(CartItem item) {

        BigDecimal lineTotal =
                item.getUnitPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        item.getQuantity()
                                )
                        );

        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal
        );
    }
}