package com.example.order_service.service.impl;

import com.ecommerce.common.dto.CartItemResponse;
import com.ecommerce.common.dto.CartResponse;
import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.security.CurrentUser;
import com.ecommerce.common.security.RoleSecurity;
import com.example.order_service.client.CartClient;
import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.CreateOrderItemRequest;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.UpdateOrderRequest;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.exception.*;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.metrics.OrderMetrics;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.OrderStatusLifecycle;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    private final ProductClient productClient;

    private final OrderMapper mapper;

    private final RoleSecurity roleSecurity;

    private final CurrentUser currentUser;

    private final OrderStatusLifecycle statusLifecycle;

    private final OrderMetrics orderMetrics;

    private final CartClient cartClient;

    public OrderServiceImpl(
            OrderRepository repository,
            ProductClient productClient,
            OrderMapper mapper,
            RoleSecurity roleSecurity,
            CurrentUser currentUser,
            OrderStatusLifecycle statusLifecycle,
            OrderMetrics orderMetrics, CartClient cartClient
    ) {
        this.repository = repository;
        this.productClient = productClient;
        this.mapper = mapper;
        this.roleSecurity = roleSecurity;
        this.currentUser = currentUser;
        this.statusLifecycle = statusLifecycle;
        this.orderMetrics = orderMetrics;
        this.cartClient = cartClient;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request,
                                     Authentication authentication) {

        validateNoDuplicateProducts(request.getItems());

        Long customerId = currentUser.getUserId(authentication);

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest requestItem : request.getItems()) {

            ProductResponse product =
                    productClient.getProduct(
                            requestItem.getProductId()
                    );

            if (Boolean.FALSE.equals(product.getActive())) {

                orderMetrics.productNotAvailable();

                throw new ProductNotAvailableException(
                        requestItem.getProductId()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal lineTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    requestItem.getQuantity()
                            )
                    );

            OrderItem orderItem =
                    OrderItem.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sku(product.getSku())
                            .unitPrice(unitPrice)
                            .quantity(requestItem.getQuantity())
                            .lineTotal(lineTotal)
                            .build();

            order.addItem(orderItem);

            totalAmount = totalAmount.add(lineTotal);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = repository.save(order);

        orderMetrics.orderCreated();

        return mapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id,
                                  Authentication authentication) {

        Order order = repository
                .findById(id)
                .orElseThrow(() -> {
                    orderMetrics.orderNotFound();

                    return new OrderNotFoundException(id);
                });

        if (roleSecurity.hasRole(authentication, "ADMIN")) {

            orderMetrics.orderViewed();

            return mapper.toResponse(order);
        }

        Long currentUserId = currentUser.getUserId(authentication);

        if (!order.getCustomerId().equals(currentUserId)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this order"
            );
        }

        orderMetrics.orderViewed();

        return mapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {

        Page<OrderResponse> result =
                repository
                        .findAll(pageable)
                        .map(mapper::toResponse);

        orderMetrics.orderViewed();

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomer(Long customerId,
                                                   Pageable pageable) {

        Page<OrderResponse> result =
                repository
                        .findByCustomerId(
                                customerId,
                                pageable
                        )
                        .map(mapper::toResponse);

        orderMetrics.orderViewed();

        return result;
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id,
                                     UpdateOrderRequest request) {

        Order existingOrder =
                repository.findById(id)
                        .orElseThrow(() -> {
                            orderMetrics.orderNotFound();

                            return new OrderNotFoundException(id);
                        });

        if (existingOrder.getStatus() != OrderStatus.PENDING_PAYMENT) {

            throw new OrderNotEditableException(id);
        }

        validateNoDuplicateProducts(request.getItems());

        BigDecimal totalAmount = BigDecimal.ZERO;

        existingOrder.getItems().clear();

        for (CreateOrderItemRequest requestItem : request.getItems()) {

            ProductResponse product =
                    productClient.getProduct(
                            requestItem.getProductId()
                    );

            if (Boolean.FALSE.equals(product.getActive())) {

                orderMetrics.productNotAvailable();

                throw new ProductNotAvailableException(
                        requestItem.getProductId()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal lineTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    requestItem.getQuantity()
                            )
                    );

            OrderItem orderItem =
                    OrderItem.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sku(product.getSku())
                            .unitPrice(unitPrice)
                            .quantity(requestItem.getQuantity())
                            .lineTotal(lineTotal)
                            .build();

            existingOrder.addItem(orderItem);

            totalAmount = totalAmount.add(lineTotal);
        }

        existingOrder.setTotalAmount(totalAmount);

        Order savedOrder = repository.save(existingOrder);

        orderMetrics.orderUpdated();

        return mapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id,
                                     Authentication authentication) {

        Order order =
                repository.findById(id)
                        .orElseThrow(() -> {
                            orderMetrics.orderNotFound();

                            return new OrderNotFoundException(id);
                        });

        if (!roleSecurity.hasRole(authentication, "ADMIN")) {

            Long currentUserId = currentUser.getUserId(authentication);

            if (!order.getCustomerId()
                    .equals(currentUserId)) {

                throw new AccessDeniedException(
                        "You are not authorized to cancel this order"
                );
            }
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {

            throw new OrderAlreadyCancelledException(id);
        }

        transitionStatus(
                order,
                OrderStatus.CANCELLED
        );

        Order savedOrder = repository.save(order);

        orderMetrics.orderCancelled();

        return mapper.toResponse(savedOrder);
    }

    private void transitionStatus(Order order,
                                  OrderStatus targetStatus) {

        OrderStatus currentStatus = order.getStatus();

        if (!statusLifecycle.canTransition(
                currentStatus,
                targetStatus)) {

            orderMetrics.invalidStatusTransition();

            throw new InvalidOrderStatusTransitionException(
                    currentStatus,
                    targetStatus
            );
        }

        order.setStatus(targetStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status,
                                                 Authentication authentication,
                                                 Pageable pageable) {

        Page<OrderResponse> result;

        if (roleSecurity.hasRole(
                authentication,
                "ADMIN")) {

            result = repository
                    .findByStatus(
                            status,
                            pageable
                    )
                    .map(mapper::toResponse);

        } else {

            Long currentUserId = currentUser.getUserId(authentication);

            result = repository
                    .findByCustomerIdAndStatus(
                            currentUserId,
                            status,
                            pageable
                    )
                    .map(mapper::toResponse);
        }

        orderMetrics.orderViewed();

        return result;
    }

    private void validateNoDuplicateProducts(List<CreateOrderItemRequest> items) {

        Set<Long> productIds = new HashSet<>();

        for (CreateOrderItemRequest item : items) {

            if (!productIds.add(item.getProductId())) {
                throw new DuplicateOrderProductException(
                        item.getProductId()
                );
            }
        }
    }

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        CartResponse cart = cartClient.getCart();

        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new EmptyCartException();
        }

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemResponse cartItem : cart.items()) {

            ProductResponse product = productClient.getProduct(cartItem.productId());

            if (product == null || Boolean.FALSE.equals(product.getActive())) {

                orderMetrics.productNotAvailable();

                throw new ProductNotAvailableException(
                        cartItem.productId()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal lineTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.quantity()
                            )
                    );

            OrderItem orderItem =
                    OrderItem.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sku(product.getSku())
                            .unitPrice(unitPrice)
                            .quantity(cartItem.quantity())
                            .lineTotal(lineTotal)
                            .build();

            order.addItem(orderItem);

            totalAmount = totalAmount.add(lineTotal);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = repository.save(order);

        /*
         * Clear the cart only after
         * the order has been persisted successfully.
         */
        cartClient.clearCart();

        orderMetrics.orderCreated();

        return mapper.toResponse(savedOrder);
    }
}