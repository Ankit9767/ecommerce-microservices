package com.example.cart_service.service.impl;

import com.ecommerce.common.dto.CartResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.security.CurrentUser;
import com.example.cart_service.client.ProductServiceClient;
import com.example.cart_service.dto.AddCartItemRequest;
import com.example.cart_service.dto.UpdateCartItemRequest;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;
import com.example.cart_service.exception.CartConcurrentModificationException;
import com.example.cart_service.exception.CartItemNotFoundException;
import com.example.cart_service.exception.CartNotFoundException;
import com.example.cart_service.exception.ProductNotAvailableException;
import com.example.cart_service.mapper.CartMapper;
import com.example.cart_service.repository.CartRepository;
import com.example.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final ProductServiceClient productServiceClient;

    private final CurrentUser currentUser;

    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseGet(() ->
                                Cart.builder()
                                        .customerId(customerId)
                                        .build()
                        );

        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(AddCartItemRequest request, Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        ProductResponse product = productServiceClient.getProduct(request.productId());

        if (product == null || Boolean.FALSE.equals(product.getActive())) {

            throw new ProductNotAvailableException(
                    request.productId()
            );
        }

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
                                        .equals(request.productId())
                        )
                        .findFirst()
                        .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + request.quantity()
            );

        } else {

            CartItem item = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .sku(product.getSku())
                    .quantity(request.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            cart.addItem(item);
        }

        try {

            Cart savedCart = cartRepository.saveAndFlush(cart);

            return cartMapper.toResponse(savedCart);

        } catch (ObjectOptimisticLockingFailureException ex) {

            throw new CartConcurrentModificationException(
                    customerId
            );
        }
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long productId,
                                   UpdateCartItemRequest request,
                                   Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseThrow(() ->
                                new CartNotFoundException(customerId)
                        );

        CartItem item =
                cart.getItems()
                        .stream()
                        .filter(cartItem ->
                                cartItem.getProductId()
                                        .equals(productId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new CartItemNotFoundException(productId)
                        );

        /*
         * Verify that the product is still active.
         */
        ProductResponse product = productServiceClient.getProduct(productId);

        if (product == null || Boolean.FALSE.equals(product.getActive())) {

            throw new ProductNotAvailableException(productId);
        }

        /*
         * Update quantity only.
         *
         * Price/name/SKU remain the values captured
         * when the item was added.
         */
        item.setQuantity(request.quantity());

        try {

            Cart savedCart = cartRepository.saveAndFlush(cart);

            return cartMapper.toResponse(savedCart);

        } catch (ObjectOptimisticLockingFailureException ex) {

            throw new CartConcurrentModificationException(
                    customerId
            );
        }
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long productId,
                                   Authentication authentication) {

        Long customerId =
                currentUser.getUserId(authentication);
        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseThrow(() ->
                                new CartNotFoundException(customerId)
                        );

        CartItem item =
                cart.getItems()
                        .stream()
                        .filter(cartItem ->
                                cartItem.getProductId()
                                        .equals(productId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new CartItemNotFoundException(productId)
                        );

        cart.removeItem(item);

        try {

            Cart savedCart = cartRepository.saveAndFlush(cart);

            return cartMapper.toResponse(savedCart);

        } catch (ObjectOptimisticLockingFailureException ex) {

            throw new CartConcurrentModificationException(
                    customerId
            );
        }
    }

    @Override
    @Transactional
    public CartResponse clearCart(Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        Cart cart =
                cartRepository.findByCustomerId(customerId)
                        .orElseThrow(() ->
                                new CartNotFoundException(customerId)
                        );

        cart.getItems().clear();

        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CartResponse> getAllCarts(Pageable pageable) {

        return cartRepository
                .findAll(pageable)
                .map(cartMapper::toResponse);
    }
}