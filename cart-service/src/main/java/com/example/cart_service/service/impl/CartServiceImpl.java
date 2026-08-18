package com.example.cart_service.service.impl;

import com.ecommerce.common.dto.CartResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
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
import com.example.cart_service.metrics.CartMetrics;
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

    private final CartMetrics cartMetrics;

    private final CartPersistenceService cartPersistenceService;

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

        cartMetrics.cartViewed();

        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse addItem(AddCartItemRequest request,
                                Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        ProductResponse product;

        try {

            product = productServiceClient.getProduct(request.productId());

        } catch (RemoteResourceNotFoundException ex) {

            cartMetrics.productUnavailable();

            throw new ProductNotAvailableException(
                    request.productId()
            );
        }

        if (Boolean.FALSE.equals(product.getActive())) {

            cartMetrics.productUnavailable();

            throw new ProductNotAvailableException(
                    request.productId()
            );
        }

        return cartPersistenceService.addItem(
                customerId,
                product.getId(),
                product.getName(),
                product.getSku(),
                request.quantity(),
                product.getPrice()
        );
    }

    @Override
    public CartResponse updateItem(Long productId,
                                   UpdateCartItemRequest request,
                                   Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        ProductResponse product;

        try {

            product = productServiceClient.getProduct(productId);

        } catch (RemoteResourceNotFoundException ex) {

            cartMetrics.productUnavailable();

            throw new ProductNotAvailableException(
                    productId
            );
        }

        if (Boolean.FALSE.equals(product.getActive())) {

            cartMetrics.productUnavailable();

            throw new ProductNotAvailableException(
                    productId
            );
        }

        return cartPersistenceService.updateItem(
                customerId,
                productId,
                request.quantity()
        );
    }

    @Override
    public CartResponse removeItem(Long productId,
                                   Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        return cartPersistenceService.removeItem(
                customerId,
                productId
        );
    }

    @Override
    public CartResponse clearCart(Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        return cartPersistenceService.clearCart(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CartResponse> getAllCarts(Pageable pageable) {

        return cartRepository
                .findAll(pageable)
                .map(cartMapper::toResponse);
    }
}