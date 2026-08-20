package com.example.cart_service.service.impl;

import com.ecommerce.common.dto.CartResponse;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;
import com.example.cart_service.exception.CartConcurrentModificationException;
import com.example.cart_service.exception.CartItemNotFoundException;
import com.example.cart_service.exception.CartNotFoundException;
import com.ecommerce.common.events.CartCheckedOutEvent;
import com.example.cart_service.kafka.CartEventProducer;
import com.example.cart_service.mapper.CartMapper;
import com.example.cart_service.metrics.CartMetrics;
import com.example.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartPersistenceService {

    private final CartRepository cartRepository;

    private final CartMapper cartMapper;

    private final CartMetrics cartMetrics;

    private final CartEventProducer cartEventProducer;


    @Transactional
    public CartResponse addItem(Long customerId,
                                Long productId,
                                String productName,
                                String sku,
                                Integer quantity,
                                java.math.BigDecimal unitPrice) {

        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseGet(() ->
                                Cart.builder()
                                        .customerId(customerId)
                                        .build()
                        );

        CartItem existingItem =
                cart.getItems()
                        .stream()
                        .filter(item ->
                                item.getProductId()
                                        .equals(productId)
                        )
                        .findFirst()
                        .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity() + quantity
            );

        } else {

            CartItem item = CartItem.builder()
                    .productId(productId)
                    .productName(productName)
                    .sku(sku)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .build();

            cart.addItem(item);
        }

        Cart savedCart = saveCart(cart, customerId);

        cartMetrics.itemAdded();

        return cartMapper.toResponse(savedCart);
    }


    @Transactional
    public CartResponse updateItem(Long customerId,
                                   Long productId,
                                   Integer quantity) {

        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseThrow(() -> {
                            cartMetrics.cartNotFound();

                            return new CartNotFoundException(
                                    customerId
                            );
                        });

        CartItem item =
                cart.getItems()
                        .stream()
                        .filter(cartItem ->
                                cartItem.getProductId()
                                        .equals(productId)
                        )
                        .findFirst()
                        .orElseThrow(() -> {
                            cartMetrics.itemNotFound();

                            return new CartItemNotFoundException(
                                    productId
                            );
                        });

        item.setQuantity(quantity);

        Cart savedCart = saveCart(cart, customerId);

        cartMetrics.itemUpdated();

        return cartMapper.toResponse(savedCart);
    }


    @Transactional
    public CartResponse removeItem(Long customerId,
                                   Long productId) {

        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseThrow(() -> {
                            cartMetrics.cartNotFound();

                            return new CartNotFoundException(
                                    customerId
                            );
                        });

        CartItem item =
                cart.getItems()
                        .stream()
                        .filter(cartItem ->
                                cartItem.getProductId()
                                        .equals(productId)
                        )
                        .findFirst()
                        .orElseThrow(() -> {
                            cartMetrics.itemNotFound();

                            return new CartItemNotFoundException(
                                    productId
                            );
                        });

        cart.removeItem(item);

        Cart savedCart = saveCart(cart, customerId);

        cartMetrics.itemRemoved();

        return cartMapper.toResponse(savedCart);
    }


    @Transactional
    public CartResponse clearCart(Long customerId) {

        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseThrow(() ->
                                new CartNotFoundException(
                                        customerId
                                )
                        );

        cart.getItems().clear();

        Cart savedCart = cartRepository.save(cart);

        cartEventProducer.publish(
                CartCheckedOutEvent.of(customerId)
        );

        return cartMapper.toResponse(savedCart);
    }

    private Cart saveCart(Cart cart,
                          Long customerId) {

        try {

            return cartRepository.saveAndFlush(cart);

        } catch (ObjectOptimisticLockingFailureException ex) {

            cartMetrics.concurrentModification();

            throw new CartConcurrentModificationException(
                    customerId
            );
        }
    }
}
