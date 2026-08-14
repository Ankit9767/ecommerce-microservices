package com.example.order_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.security.CurrentUser;
import com.ecommerce.common.security.RoleSecurity;
import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.CreateOrderItemRequest;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.UpdateOrderRequest;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.exception.*;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.OrderStatusLifecycle;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    private final ProductClient productClient;

    private final MeterRegistry meterRegistry;

    private final OrderMapper mapper;

    private final RoleSecurity roleSecurity;

    private final CurrentUser currentUser;

    private final OrderStatusLifecycle statusLifecycle;

    public OrderServiceImpl(
            OrderRepository repository,
            ProductClient productClient,
            MeterRegistry meterRegistry,
            OrderMapper mapper, RoleSecurity roleSecurity, CurrentUser currentUser, OrderStatusLifecycle statusLifecycle
    ) {
        this.repository = repository;
        this.productClient = productClient;
        this.meterRegistry = meterRegistry;
        this.mapper = mapper;
        this.roleSecurity = roleSecurity;
        this.currentUser = currentUser;
        this.statusLifecycle = statusLifecycle;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request,
                                     Authentication authentication) {

        Timer.Sample sample = Timer.start(meterRegistry);

        try {

            Long customerId = currentUser.getUserId(authentication);

            Order order = Order.builder()
                    .customerId(customerId)
                    .status(OrderStatus.PENDING_PAYMENT)
                    .totalAmount(BigDecimal.ZERO)
                    .build();

            BigDecimal totalAmount =
                    BigDecimal.ZERO;

            for (CreateOrderItemRequest requestItem :
                    request.getItems()) {

                ProductResponse product =
                        productClient.getProduct(
                                requestItem.getProductId()
                        );

                if (product == null ||
                        Boolean.FALSE.equals(product.getActive())) {

                    throw new IllegalStateException(
                            "Product is not available: "
                                    + requestItem.getProductId()
                    );
                }

                BigDecimal unitPrice =
                        product.getPrice();

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

                totalAmount =
                        totalAmount.add(lineTotal);
            }

            order.setTotalAmount(totalAmount);

            Order savedOrder =
                    repository.save(order);

            Counter.builder("orders.created.total")
                    .description("Total orders created")
                    .register(meterRegistry)
                    .increment();

            return mapper.toResponse(savedOrder);

        } finally {

            sample.stop(
                    Timer.builder("order.processing.time")
                            .description("Order creation time")
                            .register(meterRegistry)
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id,
                                  Authentication authentication) {

        Order order = repository
                .findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id)
                );

        if (roleSecurity.hasRole(authentication, "ADMIN")) {
            return mapper.toResponse(order);
        }

        Long currentUserId = currentUser.getUserId(authentication);

        if (!order.getCustomerId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You are not authorized to access this order"
            );
        }

        return mapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {

        return repository
                .findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id,
                                     UpdateOrderRequest request) {

        Order existingOrder = repository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id)
                );

        if (existingOrder.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new OrderNotEditableException(id);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Remove existing items.
        existingOrder.getItems().clear();

        for (CreateOrderItemRequest requestItem : request.getItems()) {

            ProductResponse product =
                    productClient.getProduct(
                            requestItem.getProductId()
                    );

            if (product == null ||
                    Boolean.FALSE.equals(product.getActive())) {

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

            OrderItem orderItem = OrderItem.builder()
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

        return mapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id,
                                     Authentication authentication) {

        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id)
                );

        if (!roleSecurity.hasRole(authentication, "ADMIN")) {

            Long currentUserId = currentUser.getUserId(authentication);

            if (!order.getCustomerId().equals(currentUserId)) {

                throw new AccessDeniedException(
                        "You are not authorized to cancel this order"
                );
            }
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {

            throw new OrderAlreadyCancelledException(id);
        }

        transitionStatus(order, OrderStatus.CANCELLED);

        Order savedOrder = repository.save(order);

        return mapper.toResponse(savedOrder);
    }

    private void transitionStatus(
            Order order,
            OrderStatus targetStatus) {

        OrderStatus currentStatus = order.getStatus();

        if (!statusLifecycle.canTransition(
                currentStatus,
                targetStatus)) {

            throw new InvalidOrderStatusTransitionException(
                    currentStatus,
                    targetStatus
            );
        }

        order.setStatus(targetStatus);
    }
}