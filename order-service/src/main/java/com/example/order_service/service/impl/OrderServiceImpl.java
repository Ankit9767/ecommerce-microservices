package com.example.order_service.service.impl;

import com.ecommerce.common.dto.*;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.events.*;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.ecommerce.common.security.CurrentUser;
import com.ecommerce.common.security.RoleSecurity;
import com.example.order_service.client.CartClient;
import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.CreateOrderFromCartRequest;
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
import com.example.order_service.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    private final OrderPersistenceService orderPersistenceService;

    private final OrderInventoryHelperService orderInventoryHelperService;

    private final OutboxService outboxService;

    public OrderServiceImpl(
            OrderRepository repository,
            ProductClient productClient,
            OrderMapper mapper,
            RoleSecurity roleSecurity,
            CurrentUser currentUser,
            OrderStatusLifecycle statusLifecycle,
            OrderMetrics orderMetrics, CartClient cartClient, OrderPersistenceService orderPersistenceService, OrderInventoryHelperService orderInventoryHelperService, OutboxService outboxService
    ) {
        this.repository = repository;
        this.productClient = productClient;
        this.mapper = mapper;
        this.roleSecurity = roleSecurity;
        this.currentUser = currentUser;
        this.statusLifecycle = statusLifecycle;
        this.orderMetrics = orderMetrics;
        this.cartClient = cartClient;
        this.orderPersistenceService = orderPersistenceService;
        this.orderInventoryHelperService = orderInventoryHelperService;
        this.outboxService = outboxService;
    }

    private void writeOrderCreatedToOutbox(Order order) {

        List<OrderItemDto> items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {

            items.add(OrderItemDto.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .sku(item.getSku())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .lineTotal(item.getLineTotal())
                    .build());
        }

        OrderCreatedEvent event =
                OrderCreatedEvent.builder()
                        .eventType(EventType.ORDER_CREATED)
                        .orderId(order.getId())
                        .customerId(order.getCustomerId())
                        .recipientEmail(order.getCustomerEmail())
                        .currency(order.getCurrency())
                        .paymentMethod(order.getPaymentMethod())
                        .totalAmount(order.getTotalAmount())
                        .items(items)
                        .build();

        outboxService.saveOrderCreatedEvent(event);
    }

    private void writeOrderPaidToOutbox(Order order) {

        List<OrderItemDto> items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {

            items.add(
                    OrderItemDto.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .sku(item.getSku())
                            .unitPrice(item.getUnitPrice())
                            .quantity(item.getQuantity())
                            .lineTotal(item.getLineTotal())
                            .build()
            );
        }

        OrderPaidEvent event =
                OrderPaidEvent.builder()
                        .eventType(EventType.ORDER_PAID)
                        .orderId(order.getId())
                        .customerId(order.getCustomerId())
                        .customerEmail(order.getCustomerEmail())
                        .currency(order.getCurrency())
                        .paymentMethod(order.getPaymentMethod())
                        .totalAmount(order.getTotalAmount())
                        .items(items)
                        .build();

        outboxService.saveOrderPaidEvent(event);
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request,
                                     Authentication authentication) {

        validateNoDuplicateProducts(request.getItems());

        Long customerId = currentUser.getUserId(authentication);

        String customerEmail = currentUser.getEmail(authentication);

        Order order = Order.builder()
                .customerId(customerId)
                .customerEmail(customerEmail)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .paymentMethod(request.getPaymentMethod())
                .currency(request.getCurrency())
                .reservationId(UUID.randomUUID())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest requestItem : request.getItems()) {

            ProductResponse product;

            try {

                product = productClient.getProduct(requestItem.getProductId());

            } catch (RemoteResourceNotFoundException ex) {

                orderMetrics.productNotAvailable();

                throw new ProductNotAvailableException(
                        requestItem.getProductId()
                );
            }

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

        orderInventoryHelperService.reserveInventory(order);

        try {
            OrderResponse response =
                    orderPersistenceService.createOrder(order);

            writeOrderCreatedToOutbox(order);

            return response;

        } catch (RuntimeException ex) {

            orderInventoryHelperService.releaseInventory(order.getItems(), order.getReservationId());

            throw ex;
        }
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

        List<OrderItem> existingItems =
                new ArrayList<>(
                        existingOrder.getItems()
                );

        List<OrderItem> newItems = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest requestItem : request.getItems()) {

            ProductResponse product;

            try {

                product = productClient.getProduct(requestItem.getProductId());

            } catch (RemoteResourceNotFoundException ex) {

                orderMetrics.productNotAvailable();

                throw new ProductNotAvailableException(
                        requestItem.getProductId()
                );
            }

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

            newItems.add(orderItem);

            totalAmount = totalAmount.add(lineTotal);
        }

        /*
         * Reserve only the additional inventory required
         * by the updated order.
         */
        List<OrderInventoryHelperService.InventoryAdjustment> reservations =
                orderInventoryHelperService.reserveInventoryForUpdate(
                        existingItems,
                        newItems,
                        existingOrder.getReservationId()
                );

        try {

            existingOrder.getItems().clear();

            for (OrderItem item : newItems) {
                existingOrder.addItem(item);
            }

            existingOrder.setTotalAmount(totalAmount);

            OrderResponse response =
                    orderPersistenceService.updateOrder(
                            existingOrder
                    );

            /*
             * The database update succeeded.
             *
             * Now release inventory that the new order
             * no longer requires.
             */
            orderInventoryHelperService.releaseReducedInventory(existingItems,
                    newItems,
                    existingOrder.getReservationId()
            );

            return response;

        } catch (RuntimeException ex) {

            /*
             * Database update failed.
             *
             * Restore only the additional inventory that
             * was reserved for this update.
             *
             * The original reservation remains untouched.
             */
            orderInventoryHelperService.releaseInventoryAdjustments(reservations);

            throw ex;
        }
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

        orderInventoryHelperService.releaseInventory(order.getItems(),
                order.getReservationId()
        );

        try {

            writeOrderCancelledToOutbox(order);

        } catch (JsonProcessingException ex) {

            log.error("Failed to write order-cancelled event to outbox for order {}",
                    order.getId(), ex);
        }

        orderMetrics.orderCancelled();

        return mapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse handlePaymentCompleted(PaymentEvent event) {

        Order order =
                repository.findByIdWithItems(event.getOrderId())
                        .orElseThrow(() -> {
                            orderMetrics.orderNotFound();

                            return new OrderNotFoundException(
                                    event.getOrderId()
                            );
                        });

        if (order.getStatus() == OrderStatus.PAID) {

            return mapper.toResponse(order);
        }

        orderInventoryHelperService.confirmReservations(order);

        transitionStatus(
                order,
                OrderStatus.PAID
        );

        OrderResponse response = orderPersistenceService.updateOrder(order);

        writeOrderPaidToOutbox(order);

        /*
         * Commit the stock that was reserved at order creation time
         * (external call - happens outside the DB transaction).
         */
        orderMetrics.orderPaid();

        return response;
    }

    @Override
    @Transactional
    public OrderResponse handlePaymentFailed(PaymentEvent event) {

        Order order =
                repository.findByIdWithItems(event.getOrderId())
                        .orElseThrow(() -> {

                            orderMetrics.orderNotFound();

                            return new OrderNotFoundException(
                                    event.getOrderId()
                            );
                        });

        /*
         * This PAYMENT_FAILED event was generated because
         * the order itself was cancelled.
         *
         * The order is already CANCELLED, so there is no
         * additional state transition or inventory action.
         */
        if (order.getStatus() == OrderStatus.CANCELLED) {

            log.info(
                    "Payment {} failed for already cancelled order {}. " +
                            "No further order action required.",
                    event.getPaymentId(),
                    order.getId()
            );

            return mapper.toResponse(order);
        }

        /*
         * This protects against an unexpected PAYMENT_FAILED
         * event that was not caused by order cancellation.
         */
        log.warn(
                "Payment failed for order {} while order is in status {}. " +
                        "No automatic order transition performed.",
                order.getId(),
                order.getStatus()
        );

        return mapper.toResponse(order);
    }

    private void writeOrderCancelledToOutbox(Order order) throws JsonProcessingException {

        OrderCancelledEvent event =
                OrderCancelledEvent.builder()
                        .eventType(EventType.ORDER_CANCELLED)
                        .orderId(order.getId())
                        .customerId(order.getCustomerId())
                        .recipientEmail(order.getCustomerEmail())
                        .reason("Order cancelled by user or admin")
                        .build();

        outboxService.saveOrderCancelledEvent(event);
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
    public OrderResponse createOrderFromCart(CreateOrderFromCartRequest request,
                                             Authentication authentication) {

        Long customerId = currentUser.getUserId(authentication);

        String customerEmail = currentUser.getEmail(authentication);

        CartResponse cart;

        try {

            cart = cartClient.getCart();

        } catch (RemoteResourceNotFoundException ex) {

            throw new EmptyCartException();
        }

        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new EmptyCartException();
        }

        Order order = Order.builder()
                .customerId(customerId)
                .customerEmail(customerEmail)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .paymentMethod(request.paymentMethod())
                .currency(request.currency())
                .reservationId(UUID.randomUUID())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemResponse cartItem : cart.items()) {

            ProductResponse product;

            try {

                product = productClient.getProduct(cartItem.productId());

            } catch (RemoteResourceNotFoundException ex) {

                orderMetrics.productNotAvailable();

                throw new ProductNotAvailableException(
                        cartItem.productId()
                );
            }

            if (Boolean.FALSE.equals(product.getActive())) {

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

        orderInventoryHelperService.reserveInventory(order);

        try {

            OrderResponse savedOrder =
                    orderPersistenceService.createOrderFromCart(order);

            writeOrderCreatedToOutbox(order);

            cartClient.clearCart();

            return savedOrder;

        } catch (RuntimeException ex) {

            orderInventoryHelperService.releaseInventory(
                    order.getItems(),
                    order.getReservationId()
            );

            throw ex;
        }
    }
}