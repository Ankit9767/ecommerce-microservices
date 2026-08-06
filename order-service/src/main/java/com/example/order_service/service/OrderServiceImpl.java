package com.example.order_service.service;

import com.ecommerce.common.dto.PaymentRequest;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.PaymentCompletedEvent;
import com.example.order_service.client.PaymentClient;
import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.CreateOrderRequest;
import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.exception.OrderNotFoundException;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.kafka.OrderKafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    private final ProductClient productClient;

    private final PaymentClient paymentClient;

    private final OrderKafkaProducer orderKafkaProducer;

    private final MeterRegistry meterRegistry;

    private final  OutboxService outboxService;

    public OrderServiceImpl(OrderRepository repository, ProductClient productClient, PaymentClient paymentClient, OrderKafkaProducer orderKafkaProducer, MeterRegistry meterRegistry, OutboxService outboxService) {
        this.repository = repository;
        this.productClient = productClient;
        this.paymentClient = paymentClient;
        this.orderKafkaProducer = orderKafkaProducer;
        this.meterRegistry = meterRegistry;
        this.outboxService = outboxService;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) throws JsonProcessingException {

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            ProductResponse product = productClient.getProduct(request.getProductId());

            BigDecimal totalAmount = product.price().multiply(BigDecimal.valueOf(request.getQuantity()));

            Order order = Order.builder().productId(product.id()).customerId(request.getCustomerId()).quantity(request.getQuantity()).totalAmount(totalAmount).status(OrderStatus.PENDING_PAYMENT).build();

            Order savedOrder = repository.save(order);

            Counter.builder("orders.created.total")
                    .description("Total orders created")
                    .register(meterRegistry)
                    .increment();

            OrderCreatedEvent event = OrderCreatedEvent.builder().orderId(savedOrder.getId()).customerId(savedOrder.getCustomerId()).productId(savedOrder.getProductId()).quantity(savedOrder.getQuantity()).amount(savedOrder.getTotalAmount()).paymentMethod(request.getPaymentMethod()).build();

            //using when kafka publishes a event
//        orderKafkaProducer.publishOrderCreated(event);

            // using when kafka is not called directly and called it using the outbox transaction
            outboxService.saveOrderCreatedEvent(event);

            //when using open feign :

//        PaymentRequest paymentRequest = new PaymentRequest();
//        paymentRequest.setOrderId(savedOrder.getId());
//        paymentRequest.setAmount(savedOrder.getTotalAmount());
//        paymentRequest.setPaymentMethod(request.getPaymentMethod());
//
//        PaymentResponse paymentResponse = processPaymentWithRetry(paymentRequest);
//
//        if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {
//            savedOrder.setStatus(OrderStatus.CONFIRMED);
//        } else {
//            savedOrder.setStatus(OrderStatus.PENDING_PAYMENT);
//        }
//
//        repository.save(savedOrder);

            return new OrderResponse(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getCustomerId(), savedOrder.getQuantity(), savedOrder.getTotalAmount(), savedOrder.getStatus(), savedOrder.getCreatedAt());
        }
        finally {
            sample.stop(
                    Timer.builder("order.processing.time")
                            .description("Order creation time")
                            .register(meterRegistry)
            );
        }

    }

    @Retry(name = "paymentRetry", fallbackMethod = "paymentRetryFallback")
    public PaymentResponse processPaymentWithRetry(PaymentRequest request) {
        return paymentClient.processPayment(request);
    }

    public OrderResponse paymentFallback(CreateOrderRequest request, Throwable ex) {
        ProductResponse product = productClient.getProduct(request.getProductId());

        BigDecimal totalAmount = product.price().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder().productId(product.id()).customerId(request.getCustomerId()).quantity(request.getQuantity()).totalAmount(totalAmount).status(OrderStatus.PENDING_PAYMENT).build();

        Order savedOrder = repository.save(order);

        Counter.builder("orders.created.total")
                .description("Total Orders Created")
                .register(meterRegistry)
                .increment();

        return new OrderResponse(savedOrder.getId(), savedOrder.getProductId(), savedOrder.getCustomerId(), savedOrder.getQuantity(), savedOrder.getTotalAmount(), savedOrder.getStatus(), savedOrder.getCreatedAt());
    }

    public PaymentResponse paymentRetryFallback(PaymentRequest request, Exception ex) {

        System.out.println("Retry exhausted.");

        return PaymentResponse.builder().orderId(request.getOrderId()).amount(request.getAmount()).paymentMethod(request.getPaymentMethod()).status(PaymentStatus.PENDING).transactionId(null).build();
    }

    @Override
    public Order getOrder(Long id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, Order updated) {

        Order existing = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        existing.setProductId(updated.getProductId());
        existing.setCustomerId(updated.getCustomerId());
        existing.setQuantity(updated.getQuantity());
        existing.setTotalAmount(updated.getTotalAmount());
        existing.setStatus(updated.getStatus());

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {

        if (!repository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }

        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        Order order = repository.findById(event.getOrderId()).orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));

        if (event.getPaymentStatus() == PaymentStatus.SUCCESS) {

            order.setStatus(OrderStatus.CONFIRMED);

            Counter.builder("orders.confirmed.total")
                    .description("Confirmed Orders")
                    .register(meterRegistry)
                    .increment();

        } else {

            order.setStatus(OrderStatus.PAYMENT_FAILED);

            Counter.builder("orders.payment.failed.total")
                    .description("Orders whose payment failed")
                    .register(meterRegistry)
                    .increment();

        }

        repository.save(order);

        System.out.println("Order {} updated to {} " + order.getId() + order.getStatus());

    }

}
