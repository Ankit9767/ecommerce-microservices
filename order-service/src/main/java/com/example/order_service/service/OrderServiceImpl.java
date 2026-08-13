package com.example.order_service.service;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.CreateOrderItemRequest;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.exception.OrderNotFoundException;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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

    public OrderServiceImpl(
            OrderRepository repository,
            ProductClient productClient,
            MeterRegistry meterRegistry,
            OrderMapper mapper
    ) {
        this.repository = repository;
        this.productClient = productClient;
        this.meterRegistry = meterRegistry;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Timer.Sample sample =
                Timer.start(meterRegistry);

        try {

            Order order = Order.builder()
                    .customerId(request.getCustomerId())
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
                                .quantity(
                                        requestItem.getQuantity()
                                )
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
    public OrderResponse getOrder(Long id) {

        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id)
                );

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
    public List<OrderResponse> getOrdersByCustomer(
            Long customerId) {

        return repository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}