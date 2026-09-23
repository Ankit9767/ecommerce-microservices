# E-Commerce Microservices Architecture

A comprehensive, production-ready microservices platform for building scalable e-commerce solutions. This project demonstrates enterprise-level architecture patterns, service communication, distributed tracing, monitoring, and Kubernetes deployment strategies.

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Microservices](#microservices)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Service Configuration](#service-configuration)
- [Deployment](#deployment)
- [Monitoring & Observability](#monitoring--observability)
- [Database](#database)
- [Message Queue](#message-queue)
- [API Gateway](#api-gateway)
- [Inter-Service Communication](#inter-service-communication)
- [Security](#security)
- [Payment Gateway Integration](#payment-gateway-integration)
- [Cloudflare Tunnel Setup](#cloudflare-tunnel-setup)
- [Razorpay Webhook Integration](#razorpay-webhook-integration)
- [Troubleshooting](#troubleshooting)

---

## Overview

This is a full-featured e-commerce microservices platform built with Spring Boot and Spring Cloud. The architecture follows industry best practices for distributed systems including:

- **Service Discovery**: Eureka for dynamic service registration
- **API Gateway**: Spring Cloud Gateway for unified entry point
- **Event-Driven**: Apache Kafka for asynchronous inter-service communication
- **Resilience**: Circuit breakers, retries, and bulkhead patterns
- **Observability**: Distributed tracing (Zipkin), metrics (Prometheus), and monitoring (Grafana)
- **Container Orchestration**: Kubernetes-ready with Helm charts
- **Security**: JWT authentication, service-to-service authentication, OAuth2 resource server

**Project Version**: 0.0.1-SNAPSHOT  
**Java Version**: 21  
**Spring Boot Version**: 3.3.12  
**Spring Cloud Version**: 2023.0.3

---

## 🔧 Complete System Working Summary

### **How the Entire System Works - End-to-End**

#### **1. STARTUP & SERVICE REGISTRATION PHASE**

**When services start**:
1. **Eureka Server starts first** on port 8761
   - Initializes peer awareness configuration
   - Prepares to accept service registrations
   - Starts heartbeat listener (every 30 seconds)

2. **Each microservice boots up**:
   - Spring Boot initializes the application context
   - Loads `application.properties` and configuration files
   - Initializes databases (JPA creates tables via Hibernate)
   - Loads Kafka consumer listeners
   - Connects to MySQL database via HikariCP connection pool
   - Initializes Feign clients for inter-service communication
   - **Registers with Eureka Server**:
     - Sends service name, IP, port, health check URL
     - Eureka stores in registry with metadata
     - Service becomes discoverable to other services
     - Heartbeat sent every 30 seconds to maintain registration

3. **API Gateway starts last**:
   - Loads route configuration
   - Establishes connection to Eureka
   - Discovers all registered services
   - Caches service locations in memory
   - Ready to route requests to backend services

**Result**: All services are discoverable and ready to receive requests

---

#### **2. CLIENT REQUEST FLOW (HTTP/REST)**

**User submits order request** (`http://localhost:8087/api/orders/create`):

```
Step 1: Request enters API Gateway
├─ Client sends POST request with JWT token in Authorization header
├─ Gateway receives at port 8087
└─ Request intercepted by GatewayAuthenticationFilter

Step 2: Security Authentication
├─ GatewaySecurityExceptionHandler checks request
├─ Extracts JWT token from Authorization header
├─ JwtClaimsExtractor decodes token:
│  ├─ Verifies signature using signing key
│  ├─ Checks expiration time
│  ├─ Extracts user ID, roles, permissions
│  └─ Validates claims
├─ If token invalid/expired → return 401 Unauthorized
└─ If valid → continue with user context

Step 3: CORS Validation (CorsConfig)
├─ Checks request Origin header
├─ Matches against allowed origins list
├─ If origin not allowed → return 403 Forbidden
├─ Adds CORS response headers (Access-Control-Allow-*)
└─ Continue to routing

Step 4: Route Matching
├─ Gateway matches URL path to configured routes
├─ `/api/orders/**` → route to order-service:8088
├─ Query Eureka for available instances
├─ Load balance between instances (round-robin)
└─ Select target service instance

Step 5: Forward Request to Order Service
├─ Create HTTP request to order-service:8088/create
├─ Include all original headers (including JWT)
├─ Include request body (order details)
├─ Add trace headers for distributed tracing:
│  └─ X-Trace-ID: abc123def456 (propagated through all services)
├─ Set timeout (typically 30 seconds)
└─ Send request

Step 6: Order Service Receives Request
├─ OrderServiceApplication has @EnableFeignClients
├─ OrderController maps POST /create endpoint
├─ @RequestBody deserializes JSON to CreateOrderRequest DTO
├─ SecurityConfig validates JWT again at service level
├─ User context available via @CurrentUser annotation
└─ Service method invoked
```

---

#### **3. ORDER CREATION PROCESSING (BUSINESS LOGIC)**

```
Step 1: Validate Order Request
├─ Check customer ID is present
├─ Validate order items (not empty)
├─ Validate quantities and prices
├─ Jakarta validation annotations trigger
└─ Throw MissingCustomerIdException if invalid

Step 2: Call Dependent Services via Feign (Synchronous)
├─ CartClient.getCart(customerId)
│  └─ Calls cart-service:8090 to get cart items
│
├─ ProductClient.getProducts(productIds)
│  ├─ Calls product-service:8089
│  ├─ Gets product details, pricing, availability
│  └─ Includes JWT token via FeignSecurityInterceptor
│
├─ InventoryClient.reserveInventory(items)
│  ├─ Calls inventory-service:8091
│  ├─ Reserves stock for each item
│  ├─ Gets reservation confirmation
│  └─ If fails → CircuitBreaker activates fallback
│
└─ If any service fails:
   ├─ Resilience4j CircuitBreaker triggers
   ├─ After 3 failures in 5 seconds, circuit opens
   ├─ Subsequent calls fail immediately (don't wait)
   └─ After 1 minute wait-duration, retry attempt

Step 3: Create Order Entity
├─ New Order entity created with:
│  ├─ UUID order ID
│  ├─ Customer ID
│  ├─ Order items (OrderItem entities)
│  ├─ Total amount calculated
│  ├─ Status = PENDING
│  ├─ CreatedAt = current timestamp (via BaseEntity)
│  └─ UpdatedAt = current timestamp
│
├─ OrderItem entities created for each item:
│  ├─ Product ID
│  ├─ Quantity
│  ├─ Unit price
│  ├─ Line total
│  └─ Reservation ID from inventory service
│
└─ Entity saved to MySQL (JPA transaction)

Step 4: Save to Database (within @Transactional)
├─ Hibernate prepares SQL INSERT
├─ Order inserted into orders table
├─ OrderItems inserted into order_items table
├─ Database transaction commits
├─ If error → rollback entire transaction
└─ Both database changes preserved or nothing saved

Step 5: Publish Event (Outbox Pattern)
├─ Create OrderCreatedEvent with:
│  ├─ Order ID
│  ├─ Customer ID
│  ├─ Order items
│  ├─ Total amount
│  ├─ Created timestamp
│  └─ Event ID (UUID for idempotency)
│
├─ OutboxEvent created with:
│  ├─ Event payload (serialized to JSON)
│  ├─ Event type = "ORDER_CREATED"
│  ├─ Aggregate ID = Order ID
│  ├─ Created at = now
│  └─ Published = false
│
├─ Insert OutboxEvent into outbox table
│  └─ Done within same @Transactional as order
│
└─ Transaction commits
   ├─ Order + OutboxEvent both persist
   ├─ Guaranteed consistency
   └─ No data loss

Step 6: Return Response to API Gateway
├─ Create OrderResponse DTO with:
│  ├─ Order ID
│  ├─ Status (PENDING)
│  ├─ Order items
│  └─ Total amount
│
├─ MapStruct (1.6.3) automatically maps Order entity to DTO
├─ Serialize DTO to JSON
├─ Return HTTP 201 Created with Location header
├─ Include trace ID in response headers
└─ Send to API Gateway

Step 7: API Gateway Returns Response to Client
├─ Receives response from order-service
├─ Adds response headers
├─ Returns HTTP 201 with order details
└─ Client receives order ID
```

---

#### **4. ASYNCHRONOUS EVENT PROCESSING (KAFKA)**

**Scheduled Outbox Processor runs every 5 seconds**:

```
Step 1: OutboxScheduler checks unpublished events
├─ Query outbox table WHERE published = false
├─ Retrieve unpublished events
├─ Sort by creation time (FIFO)
└─ Process in batches (default: 100)

Step 2: For each OutboxEvent:
├─ Extract event type (ORDER_CREATED)
├─ Extract event payload (JSON)
├─ Get Kafka topic from EventType enum:
│  └─ ORDER_CREATED → topic: "order-events"
│
├─ Publish to Kafka:
│  ├─ Serialize event to JSON
│  ├─ Create Kafka ProducerRecord
│  ├─ Send to "order-events" topic
│  ├─ Partition by order ID (same order always same partition)
│  ├─ Wait for acknowledgment
│  └─ Record published offset
│
└─ Mark event as published (published = true)

Step 3: Kafka stores event
├─ Event replicated to all broker replicas
├─ Stored for retention period (typically 7 days)
└─ Multiple consumer groups can subscribe

Step 4: Inventory Service consumes ORDER_CREATED
├─ Listens to "order-events" topic
├─ KafkaListener receives message
├─ Extract event from message:
│  ├─ Order ID
│  ├─ Order items with quantities
│  └─ Event ID
│
├─ EventIdempotencyService checks:
│  ├─ Has this event ID been processed before?
│  ├─ Query ProcessedEvent table
│  ├─ If exists → already processed, skip
│  └─ If not exists → process
│
├─ Process event:
│  ├─ For each order item:
│  │  ├─ Get product ID
│  │  ├─ Get reserved quantity from event
│  │  ├─ Update inventory in database
│  │  └─ Deduct from available stock
│  │
│  └─ Create InventoryReservedEvent:
│     ├─ Order ID
│     ├─ Reservation details
│     ├─ Event ID (same as source event for correlation)
│     └─ Timestamp
│
├─ Publish InventoryReservedEvent to Kafka:
│  ├─ Follows same outbox pattern
│  └─ Published to "inventory-events" topic
│
└─ Mark OrderCreatedEvent as processed:
   ├─ Insert into ProcessedEvent table
   ├─ Store event ID + timestamp
   └─ Prevents duplicate processing if message redelivered

Step 5: Payment Service consumes ORDER_CREATED
├─ Same process as Inventory Service
├─ Creates PaymentInitiatedEvent
├─ Publishes to "payment-events" topic
└─ Waits for user to complete payment

Step 6: Notification Service consumes ORDER_CREATED
├─ Gets order details from event
├─ Formats notification template
├─ Sends email to customer (async)
├─ Logs notification in database
└─ Publishes NotificationSentEvent

Step 7: Other services consume events similarly
├─ Shipping Service: Waits for payment confirmation
├─ Reviews Service: Tracks order for review eligibility
└─ Search Service: Updates product popularity metrics

Result: Multiple services process order asynchronously
├─ No service blocks others
├─ Inventory reserved
├─ Payment initiated
├─ Customer notified
├─ All within seconds
└─ Eventual consistency achieved
```

---

#### **5. PAYMENT PROCESSING (RAZORPAY)**

**When customer initiates payment**:

```
Step 1: Client sends payment request to API Gateway
├─ POST /api/payments/checkout
├─ Includes order ID, amount, customer email
└─ JWT token in header

Step 2: API Gateway routes to Payment Service
├─ Matches route to payment-service:8086
├─ Forwards request with JWT
└─ Payment Service receives

Step 3: Payment Service creates Razorpay order
├─ Validate payment request:
│  ├─ Check order exists
│  ├─ Verify amount matches order total
│  └─ Validate customer
│
├─ Call Razorpay SDK:
│  ├─ RazorpayClient initialized with API key/secret
│  ├─ Create payment order:
│  │  ├─ Amount (in paise: amount * 100)
│  │  ├─ Currency (INR)
│  │  ├─ Receipt = Order ID
│  │  └─ Description = "Order #12345"
│  │
│  ├─ Razorpay API returns:
│  │  ├─ Order ID (razorpay_order_id)
│  │  ├─ Amount
│  │  ├─ Currency
│  │  └─ Status = CREATED
│  │
│  └─ Timeout: 30 seconds
│
├─ Save Payment entity to database:
│  ├─ Razorpay Order ID
│  ├─ Order ID (our order)
│  ├─ Amount
│  ├─ Status = INITIATED
│  ├─ Created timestamp
│  └─ Customer email/phone
│
└─ Return PaymentCheckoutResponse:
   ├─ Razorpay Order ID
   ├─ Amount
   ├─ Currency
   ├─ Customer email
   ├─ API key (public, safe for client)
   └─ Description

Step 4: Client sends response to frontend
├─ Frontend receives payment order details
├─ Initializes Razorpay payment modal
└─ Shows payment form to customer

Step 5: Customer enters payment details
├─ Customer enters card/bank details
├─ Razorpay hosted UI validates inputs
├─ Razorpay processes payment:
│  ├─ Calls issuing bank
│  ├─ Bank authenticates transaction
│  ├─ Bank approves/declines
│  └─ Razorpay receives response
│
└─ Payment either succeeds or fails

Step 6: Razorpay sends webhook callback
├─ Razorpay has our webhook URL registered:
│  └─ https://webhook.yourdomain.com/api/payments/webhook/razorpay
│
├─ Webhook payload contains:
│  ├─ Event type: "payment.captured" or "payment.failed"
│  ├─ Payment ID
│  ├─ Order ID
│  ├─ Amount
│  ├─ Status
│  └─ Created timestamp
│
├─ Webhook signature generated:
│  ├─ HMAC-SHA256(payload, webhook_secret)
│  ├─ Sent as X-Razorpay-Signature header
│  └─ Event ID sent as X-Razorpay-Event-Id header
│
└─ Webhook sent via Cloudflare Tunnel
   ├─ Razorpay server sends HTTPS POST
   ├─ Goes through Cloudflare edge network
   ├─ Cloudflared daemon receives from tunnel
   ├─ Routes to localhost:8086 (Payment Service)
   └─ Webhook reaches our service

Step 7: Payment Service receives webhook
├─ WebhookController handles POST request
├─ Extract signature from header: X-Razorpay-Signature
├─ Verify webhook authenticity:
│  ├─ Get webhook secret from config
│  ├─ Calculate expected signature: HMAC-SHA256(payload, secret)
│  ├─ Compare with received signature
│  ├─ If mismatch → SecurityException → return 403
│  └─ If match → signature verified
│
├─ Parse webhook payload JSON:
│  ├─ Extract event type
│  ├─ Extract payment details
│  ├─ Extract order ID
│  └─ Extract event ID
│
├─ Check idempotency:
│  ├─ Query ProcessedWebhook table
│  ├─ Check if event ID already processed
│  ├─ If yes → already processed, return 200 OK
│  └─ If no → process new event
│
└─ Process webhook event:

Step 8: Process payment success (payment.captured)
├─ Retrieve Payment entity from database
├─ Update payment status: CAPTURED
├─ Update payment confirmation time
├─ Save to database (within @Transactional)
│
├─ Publish PaymentSuccessEvent to Kafka:
│  ├─ Event ID (same as webhook for correlation)
│  ├─ Payment ID
│  ├─ Order ID
│  ├─ Amount
│  ├─ Status = SUCCESS
│  └─ Timestamp
│
├─ Outbox pattern:
│  ├─ Save OutboxEvent with event details
│  ├─ Commit transaction
│  └─ OutboxScheduler publishes to Kafka
│
└─ Return 200 OK to Razorpay
   └─ Razorpay marks webhook as delivered

Step 9: Order Service consumes PaymentSuccessEvent
├─ Kafka listener receives event
├─ Check if already processed (idempotency)
├─ Retrieve Order entity
├─ Update order status: PAID
├─ Update payment confirmation in order
├─ Save order
│
├─ Publish OrderPaidEvent
│  ├─ Inventory Service reserves stock
│  ├─ Shipping Service prepares shipment
│  └─ Notification Service sends confirmation
│
└─ Order moves forward in fulfillment pipeline

Step 10: Notification Service sends confirmation
├─ Consumes OrderPaidEvent
├─ Retrieves order and customer details
├─ Loads email template
├─ Replaces template variables:
│  ├─ {{customer_name}}
│  ├─ {{order_id}}
│  ├─ {{amount}}
│  ├─ {{items}}
│  └─ {{estimated_delivery}}
│
├─ Sends email via mail service
├─ Logs notification in database
└─ Customer receives confirmation

Result:
├─ Payment captured successfully
├─ Order status updated to PAID
├─ Inventory reserved
├─ Shipping prepared
├─ Customer notified
└─ Entire flow completes in seconds
```

---

#### **6. INTER-SERVICE COMMUNICATION (FEIGN CLIENTS)**

**When Order Service needs Product details**:

```
Step 1: Order Service calls ProductClient
├─ @FeignClient(name = "product-service")
├─ Service discovery via Eureka:
│  ├─ Query Eureka for "product-service" instances
│  ├─ Eureka returns list of available instances:
│  │  ├─ product-service:8089 (healthy)
│  │  ├─ product-service:8089 (replica, healthy)
│  │  └─ product-service:8089 (replica, unhealthy - excluded)
│  │
│  └─ Load balancer selects instance (round-robin)
│
├─ Create HTTP request:
│  ├─ URL: http://product-service:8089/api/products/{id}
│  ├─ Method: GET (or POST, depends on interface)
│  ├─ Add JWT token via FeignSecurityInterceptor:
│  │  └─ Authorization: Bearer <jwt_token>
│  │
│  └─ Add distributed tracing headers:
│     ├─ X-Trace-ID: abc123def456
│     ├─ X-Span-ID: xyz789abc123
│     └─ X-Parent-Span-ID: parent123
│
└─ Send request (timeout: 5 seconds)

Step 2: Request reaches Product Service
├─ ProductController receives request
├─ @RequestParam extracts parameters
├─ Validate JWT token
├─ Extract user context
├─ Service layer processes:
│  ├─ Query database for product
│  ├─ Map entity to DTO (MapStruct)
│  └─ Return DTO
│
└─ Serialize DTO to JSON

Step 3: Response sent back
├─ HTTP 200 with product details JSON
├─ Include trace headers in response
└─ Return to Order Service

Step 4: Order Service processes response
├─ Deserialize JSON to ProductResponse DTO
├─ Use product details in order creation
├─ Continue business logic
└─ If fails → FeignErrorDecoder handles error

Step 5: Error Handling with Feign
├─ If Product Service returns error (5xx):
│  ├─ FeignErrorDecoder intercepts response
│  ├─ Convert HTTP error to domain exception
│  ├─ Throw ServiceUnavailableException
│  │
│  ├─ Resilience4j CircuitBreaker catches:
│  │  ├─ Count as failure
│  │  ├─ If 50% failure rate → circuit opens
│  │  ├─ Subsequent calls fail immediately
│  │  └─ After wait-duration, try again
│  │
│  └─ Order Service can:
│     ├─ Retry the call
│     ├─ Use fallback value
│     ├─ Throw error to client
│     └─ Publish failure event
│
└─ Request timeout → CircuitBreaker timeout exception
```

---

#### **7. DISTRIBUTED TRACING (ZIPKIN & BRAVE)**

**Complete request trace across services**:

```
Step 1: Client sends request to API Gateway
├─ No trace ID yet
├─ Gateway Spring Cloud Sleuth creates:
│  ├─ Trace ID: abc123def456 (128-bit UUID)
│  ├─ Span ID: xyz789abc123 (64-bit UUID)
│  └─ Trace sent in logs: [api-gateway,abc123def456,xyz789abc123]
│
└─ Add trace headers to outgoing request:
   ├─ X-Trace-ID: abc123def456
   ├─ X-Span-ID: xyz789abc123
   └─ X-Parent-Span-ID: empty (no parent)

Step 2: Order Service receives request
├─ Extract trace headers from request
├─ Sleuth finds existing trace ID: abc123def456
├─ Create new span for order-service:
│  ├─ Trace ID: abc123def456 (same as parent)
│  ├─ Span ID: order_span_123 (new)
│  ├─ Parent Span ID: xyz789abc123 (from gateway)
│  └─ Logs: [order-service,abc123def456,order_span_123]
│
├─ Record span events:
│  ├─ Event: "started" (timestamp)
│  ├─ Event: "database.query" (duration)
│  ├─ Event: "feign.call.product" (duration)
│  ├─ Event: "kafka.publish" (duration)
│  └─ Event: "completed" (timestamp)
│
└─ Send trace data to Zipkin:
   └─ Brave reporter batches spans
      ├─ Collects spans every 1 second or 100 spans
      ├─ HTTP POST to Zipkin collector
      ├─ Sends via: micrometer-tracing-bridge-brave

Step 3: Order Service calls Product Service via Feign
├─ Create new child span for feign call:
│  ├─ Trace ID: abc123def456 (same)
│  ├─ Span ID: product_call_456 (new)
│  ├─ Parent Span ID: order_span_123
│  └─ Tags: 
│     ├─ http.method = GET
│     ├─ http.url = /api/products/1
│     ├─ span.kind = CLIENT
│     └─ peer.service.name = product-service
│
├─ Add trace headers to Feign request:
│  ├─ X-Trace-ID: abc123def456
│  ├─ X-Span-ID: product_call_456
│  └─ X-Parent-Span-ID: order_span_123
│
└─ Send request

Step 4: Product Service receives Feign request
├─ Extract trace headers
├─ Create span:
│  ├─ Trace ID: abc123def456 (continues)
│  ├─ Span ID: product_server_789
│  ├─ Parent Span ID: product_call_456
│  └─ Tags: span.kind = SERVER
│
├─ Process request, record events
├─ Return response with trace headers
└─ Report span to Zipkin

Step 5: Zipkin visualization
├─ Receives all spans from all services
├─ Builds trace tree:
│  │
│  └─ Trace: abc123def456
│     ├─ api-gateway span [0-100ms]
│     │  └─ order-service span [10-95ms]
│     │     ├─ database query [15-25ms]
│     │     ├─ product-service call [30-60ms]
│     │     │  └─ product-service processing [35-55ms]
│     │     └─ kafka publish [65-85ms]
│     │
│     ├─ inventory-service span [async, 50-150ms]
│     │  └─ database update [60-70ms]
│     │
│     └─ notification-service span [async, 80-200ms]
│        └─ email send [90-180ms]
│
├─ Dashboard shows:
│  ├─ Total request time: 200ms
│  ├─ Service latencies
│  ├─ Database query times
│  ├─ Network delays
│  └─ Bottleneck identification
│
└─ Developer views in Zipkin UI:
   ├─ http://localhost:9411
   ├─ Search by trace ID
   ├─ View trace timeline
   ├─ See service dependencies
   └─ Identify slow services
```

---

#### **8. MONITORING & METRICS (PROMETHEUS & GRAFANA)**

**Metrics collection and visualization**:

```
Step 1: Services expose metrics endpoints
├─ Spring Boot Actuator enabled
├─ Micrometer configured with Prometheus registry
├─ Metrics endpoint: /actuator/prometheus
│
├─ Metrics collected:
│  ├─ HTTP metrics:
│  │  ├─ http.server.requests (counter)
│  │  ├─ http.server.requests.seconds (timer)
│  │  └─ http.server.requests.active (gauge)
│  │
│  ├─ JVM metrics:
│  │  ├─ jvm.memory.used (bytes)
│  │  ├─ jvm.memory.max (bytes)
│  │  ├─ jvm.threads.live (count)
│  │  └─ jvm.gc.memory.promoted (bytes)
│  │
│  ├─ Database metrics:
│  │  ├─ hikaricp.connections (count)
│  │  ├─ hikaricp.connections.active (gauge)
│  │  └─ hikaricp.connections.pending (gauge)
│  │
│  ├─ Kafka metrics:
│  │  ├─ kafka.producer.record.send.total (counter)
│  │  ├─ kafka.consumer.records.consumed.total (counter)
│  │  └─ kafka.consumer.lag (gauge)
│  │
│  └─ Circuit Breaker metrics:
│     ├─ resilience4j.circuitbreaker.state (gauge: 0=CLOSED, 1=OPEN, 2=HALF_OPEN)
│     ├─ resilience4j.circuitbreaker.calls.total (counter)
│     └─ resilience4j.circuitbreaker.calls.duration (timer)
│
└─ Format: Prometheus text format
   ├─ HELP lines (metric description)
   ├─ TYPE lines (counter, gauge, histogram, summary)
   └─ Metric lines with labels and values

Step 2: Prometheus scrapes metrics
├─ Prometheus job configured for each service:
│  ├─ job_name: "order-service"
│  ├─ scrape_interval: 10s
│  ├─ metrics_path: "/actuator/prometheus"
│  ├─ static_configs.targets: "order-service:8088"
│  └─ scrape_timeout: 5s
│
├─ Every 10 seconds:
│  ├─ HTTP GET to service:port/actuator/prometheus
│  ├─ Parse response
│  ├─ Store metrics in time-series database:
│  │  ├─ Metric name (http.server.requests)
│  │  ├─ Labels (service, method, status)
│  │  ├─ Timestamp
│  │  └─ Value
│  │
│  └─ Storage in TSDB:
│     ├─ ~10 bytes per time-series
│     ├─ Typical setup: 50 services × 1000 metrics each
│     ├─ Retention: 15 days (default)
│     └─ Disk usage: ~50 GB
│
├─ Alert rules evaluated (every 10 seconds):
│  ├─ Rule: "error_rate > 5%"
│  │  └─ IF sum(rate(http.server.requests.total{status=~"5.."}[5m])) / sum(rate(http.server.requests.total[5m])) > 0.05
│  │     THEN Alert "HighErrorRate"
│  │
│  ├─ Rule: "service_down"
│  │  └─ IF up{job="order-service"} == 0
│  │     THEN Alert "ServiceDown"
│  │
│  └─ Rule: "memory_high"
│     └─ IF jvm.memory.used / jvm.memory.max > 0.9
│        THEN Alert "HighMemoryUsage"
│
└─ Alerts sent to AlertManager if triggered

Step 3: Grafana queries metrics
├─ Grafana connects to Prometheus datasource
├─ PromQL queries:
│  ├─ "rate(http.server.requests_total{job='order-service'}[5m])"
│  │  └─ Shows request rate for order-service over last 5 minutes
│  │
│  ├─ "sum(jvm.memory.used) by (instance)"
│  │  └─ Total memory used per instance
│  │
│  └─ "histogram_quantile(0.95, rate(http.server.requests_seconds_bucket[5m]))"
│     └─ 95th percentile response time
│
├─ Dashboards display metrics:
│  ├─ Application Dashboard:
│  │  ├─ Requests per second (line graph)
│  │  ├─ Error rate (gauge)
│  │  ├─ Average response time (stat)
│  │  └─ Service health status (table)
│  │
│  ├─ JVM Dashboard:
│  │  ├─ Heap memory usage (area chart)
│  │  ├─ Garbage collection frequency (graph)
│  │  ├─ Thread count (gauge)
│  │  └─ GC duration (heatmap)
│  │
│  ├─ Database Dashboard:
│  │  ├─ Connection pool utilization (gauge)
│  │  ├─ Query execution time (histogram)
│  │  ├─ Active connections (graph)
│  │  └─ Connection wait time (stat)
│  │
│  └─ Kafka Dashboard:
│     ├─ Messages produced (counter)
│     ├─ Messages consumed (counter)
│     ├─ Consumer lag (gauge)
│     └─ Topic partition distribution (bar chart)
│
└─ Grafana UI: http://localhost:3000

Step 4: AlertManager processes alerts
├─ AlertManager receives triggered alerts from Prometheus
├─ Evaluates routing rules:
│  ├─ group_by: [alertname, cluster, service]
│  │  └─ Groups similar alerts together
│  │
│  └─ Routes alerts to:
│     ├─ Email (if severity=critical)
│     ├─ Slack (if severity=warning)
│     └─ PagerDuty (if severity=critical)
│
├─ Alert example:
│  ├─ Service "order-service" is down
│  ├─ AlertManager sends:
│  │  ├─ Email to ops@company.com
│  │  ├─ Slack message to #alerts channel
│  │  └─ SMS to on-call engineer
│  │
│  └─ Alert includes:
│     ├─ Service name
│     ├─ Alert message
│     ├─ When it started
│     ├─ Metric values
│     └─ Dashboard link
│
└─ Operator acknowledges alert
   └─ Stops repeated notifications
```

---

#### **9. SECURITY FLOW (JWT & AUTHENTICATION)**

**End-to-end authentication and authorization**:

```
Step 1: User logs in
├─ POST /api/auth/login
├─ Credentials: email + password
├─ Sent to API Gateway
└─ Routed to Auth Service

Step 2: Auth Service validates credentials
├─ Query User entity from database
├─ Find user by email
├─ Get stored password hash (Bcrypt)
├─ Compare provided password with hash:
│  ├─ If match → credentials valid
│  └─ If no match → Authentication failed, return 401
│
├─ Generate JWT token:
│  ├─ Header: {
│  │  "alg": "HS256",
│  │  "typ": "JWT"
│  │ }
│  │
│  ├─ Payload: {
│  │  "sub": "user123",
│  │  "email": "user@example.com",
│  │  "name": "John Doe",
│  │  "roles": ["CUSTOMER"],
│  │  "permissions": ["read:orders", "create:orders"],
│  │  "exp": 1234567890,  (current time + 1 hour)
│  │  "iat": 1234567890,  (issued at)
│  │  "iss": "auth-service"
│  │ }
│  │
│  └─ Signature: HMAC-SHA256(header.payload, secret_key)
│
├─ Return token to client:
│  ├─ access_token (JWT)
│  ├─ token_type: "Bearer"
│  ├─ expires_in: 3600 (seconds)
│  └─ refresh_token (optional, for long-lived sessions)
│
└─ Client stores token (localStorage, sessionStorage, or httpOnly cookie)

Step 3: Client sends authenticated request
├─ Include JWT in Authorization header:
│  └─ Authorization: Bearer eyJhbGc...xyz789
│
├─ POST /api/orders/create
├─ Send to API Gateway
└─ Gateway receives request

Step 4: API Gateway validates JWT
├─ Extract token from Authorization header
├─ Remove "Bearer " prefix
├─ Parse JWT (3 parts separated by dots):
│  ├─ Header: base64decode
│  ├─ Payload: base64decode
│  └─ Signature: keep for verification
│
├─ Verify JWT signature:
│  ├─ Get signing key from config (secret_key)
│  ├─ Recalculate signature: HMAC-SHA256(header.payload, secret_key)
│  ├─ Compare calculated with received signature
│  ├─ If match → token authentic, signature verified
│  └─ If no match → token tampered, return 401 Unauthorized
│
├─ Verify token expiration:
│  ├─ Extract exp claim from payload
│  ├─ Compare with current time
│  ├─ If current_time <= exp → token valid
│  └─ If current_time > exp → token expired, return 401
│
├─ Extract user information from token:
│  ├─ User ID (sub)
│  ├─ Email
│  ├─ Roles (CUSTOMER, ADMIN, etc.)
│  ├─ Permissions (read:orders, write:orders, etc.)
│  └─ Store in SecurityContext for request
│
└─ Add to request context:
   └─ @CurrentUser annotation injects UserPrincipal into controller methods

Step 5: Order Service receives request
├─ Token already validated by gateway
├─ Order Service can optionally validate again:
│  ├─ Extract token from header
│  ├─ Verify signature (same process)
│  ├─ Verify expiration
│  └─ Defense in depth: services don't trust gateway alone
│
├─ Extract user context:
│  ├─ @CurrentUser UserPrincipal user
│  ├─ user.getId() → user ID
│  ├─ user.getRoles() → user roles
│  └─ user.getPermissions() → user permissions
│
└─ Check authorization:
   ├─ @PreAuthorize("hasRole('CUSTOMER')")
   │  └─ If user role is CUSTOMER → allowed
   │  └─ If user role is ADMIN → allowed (typically)
   │  └─ Otherwise → 403 Forbidden
   │
   ├─ @PreAuthorize("hasPermission(#orderId, 'read')")
   │  └─ If user has read permission on this order → allowed
   │  └─ Otherwise → 403 Forbidden
   │
   └─ @Secured({"ROLE_CUSTOMER", "ROLE_ADMIN"})
      └─ Similar to @PreAuthorize

Step 6: Service-to-Service Authentication (Feign)
├─ Order Service calls Inventory Service
├─ FeignSecurityInterceptor intercepts request:
│  ├─ Extract current user context
│  ├─ Create new JWT for service-to-service call:
│  │  ├─ Subject: "order-service"
│  │  ├─ Issued by: "order-service"
│  │  ├─ Includes internal flag: internal=true
│  │  └─ Signed with service secret
│  │
│  └─ Add to Feign request:
│     └─ Authorization: Bearer service_jwt_token
│
├─ Inventory Service receives Feign request:
│  ├─ Extract service token
│  ├─ Verify signature with service key
│  ├─ Check internal flag = true
│  ├─ If verified → allow service-to-service call
│  └─ If not verified → return 401
│
└─ Call proceeds with service authorization

Step 7: Token Refresh (Optional)
├─ If token expired:
│  ├─ Client sends refresh token
│  ├─ POST /api/auth/refresh
│  ├─ Auth Service validates refresh token
│  ├─ If valid → generate new access token
│  └─ Return new token to client
│
└─ Client updates Authorization header with new token

Result:
├─ Request validated at Gateway
├─ User context available in all services
├─ Service-to-service calls authenticated
├─ Unauthorized requests rejected (401)
├─ User actions checked against permissions (403)
└─ Token expiration enforced
```

---

#### **10. DATABASE TRANSACTIONS & DATA CONSISTENCY**

**How data is saved reliably**:

```
Step 1: Order Creation starts transaction
├─ @Transactional annotation marks method
├─ Spring starts database transaction
├─ Connection obtained from HikariCP pool
├─ Database isolation level set (READ_COMMITTED typical)
└─ Log sequence number recorded

Step 2: Within transaction
├─ Save Order entity:
│  ├─ Hibernate generates INSERT SQL
│  ├─ INSERT INTO orders (id, customer_id, amount, status) VALUES (...)
│  ├─ Statement prepared
│  ├─ Parameters bound
│  ├─ Execute on database
│  ├─ Order entity ID assigned
│  └─ Entity now managed by persistence context
│
├─ Save OrderItem entities:
│  ├─ For each item:
│  │  ├─ INSERT INTO order_items (...) VALUES (...)
│  │  └─ Foreign key to order_id
│  │
│  └─ All items inserted
│
├─ Update inventory:
│  ├─ UPDATE inventory SET available_qty = available_qty - ? WHERE product_id = ?
│  ├─ Quantity reduced
│  └─ Stock reserved
│
├─ Save Outbox entry:
│  ├─ INSERT INTO outbox (event_type, event_data, created_at) VALUES (...)
│  ├─ OrderCreatedEvent payload stored as JSON
│  └─ Published flag = false
│
└─ All changes in memory, not yet committed to database

Step 3: Transaction commits
├─ No errors occurred during transaction
├─ Commit point reached (end of method)
├─ Spring/Hibernate issues COMMIT
│
├─ Database flushes all changes:
│  ├─ Order inserted into orders table
│  ├─ OrderItems inserted into order_items table
│  ├─ Inventory updated
│  ├─ Outbox entry inserted
│  └─ All changes made atomic
│
├─ Transaction log updated
├─ Changes replicated to database replicas (if configured)
└─ Transaction complete

Step 4: Transaction rollback (on error)
├─ If exception occurs within transaction:
│  ├─ RuntimeException caught by @Transactional
│  ├─ Checked exceptions don't cause rollback (unless specified)
│  └─ @Transactional(rollbackFor = Exception.class) catches all
│
├─ Spring issues ROLLBACK
├─ Database undoes all changes:
│  ├─ Order not inserted
│  ├─ OrderItems not inserted
│  ├─ Inventory not changed
│  ├─ Outbox entry not saved
│  └─ Database returns to pre-transaction state
│
├─ Persistence context cleared
├─ Exception propagated to caller
└─ No partial data in database

Step 5: Concurrent request handling
├─ Multiple requests processing simultaneously
├─ Each gets own database connection from HikariCP pool
├─ Isolation prevents dirty reads:
│  ├─ Transaction 1 inserts Order A
│  ├─ Transaction 2 starts
│  ├─ Transaction 2 queries all orders
│  ├─ Only committed orders visible (depends on isolation level)
│  └─ If T1 rolls back: Order A not visible to T2
│
├─ Row-level locking for updates:
│  ├─ Transaction 1: UPDATE inventory WHERE product_id = 1
│  ├─ Database locks row
│  ├─ Transaction 2: UPDATE inventory WHERE product_id = 1
│  ├─ Transaction 2 waits for lock
│  ├─ Transaction 1 commits, releases lock
│  ├─ Transaction 2 acquires lock, proceeds
│  └─ Prevents lost updates
│
└─ Connection returned to pool after transaction

Step 6: Data consistency checks
├─ Foreign key constraints:
│  ├─ OrderItem.order_id must reference valid Order
│  ├─ If invalid reference: integrity error
│  └─ Insert prevented by database
│
├─ Unique constraints:
│  ├─ Order ID unique
│  ├─ If duplicate ID: unique constraint violation
│  └─ Insert prevented
│
├─ Check constraints:
│  ├─ Order amount > 0
│  ├─ If amount <= 0: constraint violation
│  └─ Insert prevented
│
└─ Data type validation:
   ├─ Email format validated in application
   ├─ Database also enforces if column type defined
   └─ Double validation for safety

Step 7: Outbox pattern for event publishing
├─ Both Order + OutboxEvent saved in same transaction
├─ Atomicity guaranteed: either both save or neither
├─ Prevents "order saved but event not published" scenarios
│
├─ OutboxScheduler runs every 5 seconds:
│  ├─ SELECT * FROM outbox WHERE published = false
│  ├─ For each unpublished event:
│  │  ├─ Publish to Kafka
│  │  ├─ On success: UPDATE outbox SET published = true
│  │  └─ If fails: retry next cycle (eventual consistency)
│  │
│  └─ Guarantees at-least-once delivery
│
└─ If order saved but event not yet published:
   ├─ System is in transient inconsistent state
   ├─ But OutboxScheduler will eventually publish event
   ├─ Eventual consistency achieved
   └─ No data loss
```

---

#### **11. SEARCH INDEXING (ELASTICSEARCH)**

**How products become searchable**:

```
Step 1: Product created in Product Service
├─ ProductController receives POST /products
├─ Create Product entity
├─ Save to MySQL database
├─ Publish ProductCreatedEvent to Kafka:
│  ├─ Product ID
│  ├─ Name, description
│  ├─ Price, category
│  ├─ Timestamp
│  └─ Event type: PRODUCT_CREATED
│
└─ Event goes to outbox

Step 2: Search Service consumes ProductCreatedEvent
├─ KafkaListener on product-events topic
├─ Extract event payload:
│  ├─ Product ID
│  ├─ Product name
│  ├─ Description
│  ├─ Category
│  ├─ Price
│  ├─ Availability
│  └─ Other searchable fields
│
├─ Create Elasticsearch document:
│  └─ {
│     "id": "prod_123",
│     "name": "Laptop",
│     "description": "High performance laptop...",
│     "category": "Electronics",
│     "price": 50000,
│     "availability": "in_stock",
│     "popularity_score": 0.8,
│     "rating": 4.5,
│     "tags": ["laptop", "computer", "electronics"],
│     "indexed_at": "2024-09-23T07:30:00Z"
│    }
│
├─ Index document in Elasticsearch:
│  ├─ HTTP PUT request to Elasticsearch cluster
│  ├─ POST /products/_doc/prod_123
│  ├─ Elasticsearch stores in index "products"
│  ├─ Creates inverted index for full-text search:
│  │  └─ Term "laptop" → documents containing it
│  │  └─ Term "computer" → documents containing it
│  │  └─ Term "high" → documents containing it
│  │
│  └─ Document indexed (searchable) within 1 second
│
└─ Search Service persists mapping in database

Step 3: User performs search
├─ GET /api/search?q=laptop&category=Electronics
├─ Request to API Gateway
├─ Routed to Search Service:8094
└─ Search Service receives query

Step 4: Search Service queries Elasticsearch
├─ Build Elasticsearch query:
│  └─ {
│     "query": {
│       "bool": {
│         "must": [
│           {"match": {"name": "laptop"}},  // must match
│           {"match": {"description": "laptop"}}  // or description
│         ],
│         "filter": [
│           {"term": {"category": "Electronics"}}  // exact match filter
│         ]
│       }
│     },
│     "size": 20,  // limit results
│     "sort": [
│       {"popularity_score": {"order": "desc"}}  // sort by popularity
│     ]
│    }
│
├─ Send query to Elasticsearch:
│  ├─ HTTP POST /products/_search
│  ├─ Elasticsearch parses query
│  ├─ Searches inverted index for "laptop"
│  ├─ Applies filters (category = Electronics)
│  ├─ Scores results by relevance
│  ├─ Sorts by popularity_score
│  ├─ Returns top 20 results
│  └─ Query time: typically <100ms
│
└─ Results with scores:
   └─ [{
      "id": "prod_123",
      "name": "Laptop",
      "score": 2.5,  // relevance score
      "category": "Electronics",
      "price": 50000,
      "rating": 4.5
     },
     {
      "id": "prod_456",
      "name": "Gaming Laptop",
      "score": 2.1,
      "category": "Electronics",
      "price": 75000,
      "rating": 4.8
     }]

Step 5: Return results to client
├─ Deserialize Elasticsearch response
├─ Map to SearchResult DTOs
├─ Include total hit count
├─ Include facets (for filtering):
│  ├─ Categories: Electronics (150), Computers (80)
│  └─ Price ranges: <25000 (10), 25000-50000 (45), >50000 (75)
│
├─ Return to API Gateway
└─ Client receives paginated search results

Step 6: Product updated
├─ Product Service: PUT /products/prod_123
├─ Update Product entity in MySQL
├─ Publish ProductUpdatedEvent:
│  ├─ Contains updated fields
│  ├─ Product ID
│  ├─ Changes (old and new values)
│  └─ Timestamp
│
├─ Search Service consumes event:
│  ├─ Get existing document from Elasticsearch
│  ├─ Update specific fields:
│  │  ├─ PUT /products/_doc/prod_123
│  │  └─ Updated JSON
│  │
│  └─ Document reindexed
│
└─ Updated product appears in search immediately

Step 7: Elasticsearch cluster management
├─ Sharding: Documents distributed across multiple nodes
│  ├─ Default: 5 shards for "products" index
│  ├─ Each shard on different node
│  ├─ Enables parallel search
│  └─ Scales to terabytes of data
│
├─ Replication: Each shard replicated
│  ├─ Primary shard + replica shards
│  ├─ If node fails, replica becomes primary
│  ├─ High availability
│  └─ Read scaling
│
├─ Refresh: Segments flushed to disk
│  ├─ Default: every 1 second
│  ├─ Documents searchable after refresh
│  └─ Write-optimized buffer → searchable index
│
└─ Index optimization:
   ├─ Merge small segments into larger ones
   ├─ Reduces memory and disk usage
   ├─ Improves query performance
   └─ Run during off-peak hours
```

---

#### **12. CONTAINERIZATION & KUBERNETES DEPLOYMENT**

**How services are deployed to Kubernetes**:

```
Step 1: Build Docker image
├─ Maven builds service JAR:
│  ├─ mvn clean package
│  ├─ All dependencies included
│  ├─ Creates executable JAR
│  └─ artifact: order-service-0.0.1-SNAPSHOT.jar
│
├─ Dockerfile defines image:
│  └─ FROM openjdk:21-jdk-slim
│     COPY target/order-service-0.0.1-SNAPSHOT.jar app.jar
│     ENTRYPOINT ["java", "-jar", "app.jar"]
│
├─ Build image:
│  ├─ docker build -t ecommerce/order-service:1.0 .
│  ├─ Image created with all dependencies
│  ├─ Size: ~500MB (Java 21 slim base + app)
│  └─ Image ID: sha256:abc123...
│
└─ Push to registry:
   ├─ docker push ecommerce/order-service:1.0
   ├─ Uploaded to Docker Hub or private registry
   └─ Available for K8s to pull

Step 2: Kubernetes namespace and secrets
├─ Create namespace:
│  ├─ kubectl apply -f namespace.yaml
│  ├─ namespace: ecommerce
│  └─ Isolates resources from other namespaces
│
├─ Create secrets:
│  ├─ kubectl apply -f secret.yaml
│  ├─ Stores sensitive data:
│  │  ├─ Database password
│  │  ├─ Kafka bootstrap servers
│  │  ├─ JWT secret key
│  │  ├─ Razorpay API key/secret
│  │  └─ Base64 encoded
│  │
│  └─ Mounted as environment variables or files
│
└─ Create ConfigMaps:
   ├─ kubectl apply -f configmap.yaml
   ├─ Stores configuration:
   │  ├─ Service ports
   │  ├─ Eureka URL
   │  ├─ Database connection settings
   │  └─ Feature flags
   │
   └─ Mounted as environment variables or config files

Step 3: Deploy MySQL database
├─ StatefulSet ensures data persistence
├─ kubectl apply -f mysql/statefulset.yaml
├─ Kubernetes creates:
│  ├─ PersistentVolume (10 GB storage)
│  ├─ PersistentVolumeClaim (claims storage)
│  ├─ StatefulSet pod "mysql-0" (ordered creation)
│  ├─ Persistent storage attached to pod
│  └─ Pod name: mysql-0 (consistent across restarts)
│
├─ MySQL starts:
│  ├─ Initializes database schema
│  ├─ Data persisted to PV
│  ├─ If pod dies, PV keeps data
│  ├─ New pod attached to same PV
│  └─ Data intact
│
└─ Service created:
   ├─ kubectl apply -f mysql/service.yaml
   ├─ ClusterIP: mysql.ecommerce.svc.cluster.local
   └─ Other pods resolve "mysql" to this IP

Step 4: Deploy Kafka
├─ Zookeeper StatefulSet first (coordination)
│  ├─ 3 replicas for quorum
│  ├─ Persistent storage for metadata
│  └─ Services created for each (zookeeper-0, 1, 2)
│
├─ Kafka StatefulSet (brokers)
│  ├─ 3 replicas (high availability)
│  ├─ Each pod: Kafka broker instance
│  ├─ Persistent storage for message log
│  ├─ Zookeeper coordinated cluster
│  └─ Services: kafka-0, kafka-1, kafka-2 (internal)
│
└─ Kafka cluster bootstrap:
   ├─ Brokers discover each other via Zookeeper
   ├─ Partition replication configured
   ├─ Topics auto-created if enable.auto.create.topics=true
   └─ Broker 0 becomes leader (default)

Step 5: Deploy Eureka Server
├─ Deployment created:
│  ├─ kubectl apply -f eureka/deployment.yaml
│  ├─ Single replica initially
│  └─ Can scale to 3 for high availability
│
├─ Pod specifications:
│  ├─ Image: ecommerce/eureka-server:1.0
│  ├─ Container port: 8761
│  ├─ Resource requests:
│  │  ├─ CPU: 250m (0.25 vCPU)
│  │  ├─ Memory: 512Mi (512 MB)
│  │  └─ Guarantees minimum resources
│  │
│  ├─ Resource limits:
│  │  ├─ CPU: 500m (0.5 vCPU)
│  │  ├─ Memory: 1Gi (1 GB)
│  │  └─ K8s kills pod if exceeded
│  │
│  └─ Environment variables:
│     ├─ EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
│     └─ (Eureka server doesn't register itself)
│
├─ Service created:
│  ├─ kubectl apply -f eureka/service.yaml
│  ├─ Type: ClusterIP
│  ├─ DNS: eureka.ecommerce.svc.cluster.local
│  ├─ Port: 8761
│  └─ Other services connect via this DNS
│
└─ Readiness probe:
   ├─ HTTP GET /eureka/apps
   ├─ Every 10 seconds
   ├─ If fails 3 times → pod not ready
   └─ Service endpoints exclude unready pods

Step 6: Deploy microservices
├─ For each service (order, payment, etc.):
│  ├─ kubectl apply -f order-service/deployment.yaml
│  │
│  ├─ Deployment specification:
│  │  ├─ name: order-service
│  │  ├─ replicas: 3 (high availability)
│  │  ├─ image: ecommerce/order-service:1.0
│  │  ├─ containerPort: 8088
│  │  │
│  │  ├─ Environment variables from ConfigMap:
│  │  │  ├─ SPRING_DATASOURCE_URL
│  │  │  ├─ SPRING_DATASOURCE_USERNAME
│  │  │  ├─ EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
│  │  │  └─ KAFKA_BOOTSTRAP_SERVERS
│  │  │
│  │  ├─ Environment variables from Secret:
│  │  │  ├─ SPRING_DATASOURCE_PASSWORD
│  │  │  ├─ JWT_SECRET_KEY
│  │  │  └─ RAZORPAY_KEY_SECRET
│  │  │
│  │  ├─ Resource requests (reserved):
│  │  │  ├─ CPU: 500m
│  │  │  └─ Memory: 1Gi
│  │  │
│  │  ├─ Resource limits (max):
│  │  │  ├─ CPU: 1000m (1 vCPU)
│  │  │  └─ Memory: 2Gi
│  │  │
│  │  ├─ Liveness probe (is container alive?):
│  │  │  ├─ HTTP GET /actuator/health/liveness
│  │  │  ├─ initialDelaySeconds: 30 (wait before first check)
│  │  │  ├─ periodSeconds: 10 (check every 10s)
│  │  │  ├─ timeoutSeconds: 2 (wait 2s for response)
│  │  │  ├─ failureThreshold: 3 (3 failures = restart)
│  │  │  └─ If fails → K8s kills and restarts pod
│  │  │
│  │  ├─ Readiness probe (is app ready for traffic?):
│  │  │  ├─ HTTP GET /actuator/health/readiness
│  │  │  ├─ initialDelaySeconds: 10 (wait before first check)
│  │  │  ├─ periodSeconds: 5 (check every 5s)
│  │  │  ├─ failureThreshold: 2 (2 failures = not ready)
│  │  │  └─ If fails → Service removes from endpoints (no traffic)
│  │  │
│  │  └─ Startup probe (special for slow startups):
│  │     ├─ HTTP GET /actuator/health/startup
│  │     ├─ periodSeconds: 5
│  │     ├─ failureThreshold: 12 (60 seconds max startup)
│  │     └─ After passing, liveness probe takes over
│  │
│  ├─ Kubernetes creates:
│  │  ├─ Deployment object (manages ReplicaSets)
│  │  ├─ ReplicaSet (ensures 3 pods running)
│  │  ├─ 3 Pod instances (order-service-abc123-***, etc.)
│  │  └─ Each pod has unique name
│  │
│  ├─ Service created:
│  │  ├─ Type: ClusterIP (internal DNS)
│  │  ├─ Selector: app=order-service
│  │  ├─ Port: 8088
│  │  ├─ DNS: order-service.ecommerce.svc.cluster.local
│  │  └─ Load balances across 3 pods
│  │
│  ├─ Ingress created (optional, for external access):
│  │  ├─ HTTP rule: /api/orders/* → order-service:8088
│  │  ├─ TLS certificate (https)
│  │  └─ Public DNS: api.yourdomain.com
│  │
│  └─ Rolling update:
│     ├─ maxSurge: 1 (allow 1 extra pod during update)
│     ├─ maxUnavailable: 1 (allow 1 pod down during update)
│     ├─ kubectl set image ...
│     ├─ Kills old pod, starts new pod (with new image)
│     ├─ Readiness probe waits for new pod to be ready
│     ├─ Service endpoint updated
│     ├─ Repeat for next pod
│     └─ Zero downtime deployment

Step 7: Deploy API Gateway last
├─ Deployment for api-gateway:8087
├─ 3 replicas
├─ Service type: LoadBalancer or NodePort
│  ├─ LoadBalancer: External IP from cloud provider
│  ├─ NodePort: Port on each node (31-32767 range)
│  └─ Exposes service externally
│
├─ Ingress routes external traffic:
│  ├─ External IP or domain
│  ├─ TLS termination (HTTPS)
│  ├─ DDoS protection (Cloudflare in front)
│  └─ Routes to API Gateway pods
│
└─ Services access via internal DNS:
   ├─ order-service.ecommerce.svc.cluster.local
   ├─ payment-service.ecommerce.svc.cluster.local
   └─ All service-to-service traffic internal

Step 8: Deploy monitoring stack
├─ Prometheus Deployment:
│  ├─ StatefulSet for data persistence
│  ├─ PersistentVolume for metrics storage
│  ├─ ConfigMap for prometheus.yml
│  ├─ Scrapes /actuator/prometheus from all services
│  └─ Stores metrics (15 days retention)
│
├─ Grafana Deployment:
│  ├─ Connects to Prometheus datasource
│  ├─ Loads pre-built dashboards
│  ├─ Service type: LoadBalancer or NodePort
│  ├─ Port: 3000
│  └─ Accessible: http://grafana.yourdomain.com
│
├─ AlertManager Deployment:
│  ├─ ConfigMap for alertmanager.yml
│  ├─ Receives alerts from Prometheus
│  ├─ Routes alerts (email, Slack, PagerDuty)
│  └─ Service type: ClusterIP (internal only)
│
└─ Zipkin Deployment (optional):
   ├─ Single pod (or replicated)
   ├─ In-memory or persistent storage
   ├─ Collects distributed traces
   └─ UI accessible on port 9411

Step 9: Verify deployment
├─ Check all pods running:
│  ├─ kubectl get pods -n ecommerce
│  ├─ Should show 3 replicas of each service
│  └─ STATUS: Running
│
├─ Check services DNS:
│  ├─ kubectl get svc -n ecommerce
│  ├─ Lists all services and internal IPs
│  └─ Test DNS: kubectl run -it --rm debug --image=busybox --restart=Never -- nslookup order-service
│
├─ Check service health:
│  ├─ kubectl logs -f -n ecommerce <pod-name>
│  ├─ Verify services registered with Eureka
│  └─ curl http://eureka:8761/eureka/apps
│
└─ Test inter-service communication:
   ├─ Port forward: kubectl port-forward -n ecommerce svc/api-gateway 8087:8087
   ├─ Test: curl http://localhost:8087/api/orders
   └─ Should work end-to-end

Step 10: Horizontal Pod Autoscaling (HPA)
├─ Metrics server enables CPU/memory metrics
├─ HPA monitors metrics:
│  ├─ CPU usage per pod
│  ├─ Memory usage per pod
│  └─ Custom metrics (requests per second)
│
├─ HPA rules:
│  ├─ If average CPU > 70% → scale up (+1 pod)
│  ├─ If average CPU < 30% → scale down (-1 pod)
│  ├─ Min replicas: 2
│  ├─ Max replicas: 10
│  └─ Scale-up: 1 pod per 30 seconds
│
├─ During traffic spike:
│  ├─ More requests → CPU increases
│  ├─ HPA creates new pod
│  ├─ Service load balances across more pods
│  ├─ CPU normalized
│  └─ Auto-healing

└─ Cost optimization:
   ├─ More pods when needed
   ├─ Fewer pods during low traffic
   ├─ Optimizes cluster resource usage
   ├─ Reduces cloud infrastructure costs
   └─ Maintains performance/SLAs
```

This comprehensive summary covers all aspects of how your e-commerce microservices system works from startup through deployment! Each section details the complete flow of data, requests, and processing.

---

## Project Structure

```
ecommerce-microservices/
├── api-gateway/                 # Spring Cloud Gateway - API entry point
├── auth-service/                # Authentication & Authorization service
├── order-service/               # Order management service
├── payment-service/             # Payment processing service
├── product-service/             # Product catalog service
├── cart-service/                # Shopping cart service
├── inventory-service/           # Inventory management service
├── notification-service/        # Email/SMS notifications service
├── shipping-service/            # Shipping & delivery service
├── search-service/              # Elasticsearch-based search service
├── reviews-service/             # Product reviews & ratings service
├── eureka-server/               # Service registry & discovery
├── common-library/              # Shared code, DTOs, exceptions, events
├── kubernetes/                  # K8s manifests & configurations
│   ├── mysql/                   # MySQL StatefulSet & Service
│   ├── kafka/                   # Kafka & Zookeeper StatefulSets
│   ├── eureka/                  # Eureka server deployment
│   ├── api-gateway/             # API Gateway deployment
│   ├── order-service/           # Order Service deployment
│   ├── payment-service/         # Payment Service deployment
│   ├── product-service/         # Product Service deployment
│   ├── auth-service/            # Auth Service deployment
│   ├── monitoring/              # Prometheus, Grafana, AlertManager
│   ├── namespace/               # Kubernetes namespace definitions
│   ├── common/                  # ConfigMaps, Secrets, PVs
│   ├── pod.yaml                 # Pod sample definition
│   ├── deployment.yaml          # Deployment sample with probes
│   ├── headless-service.yaml    # Headless service for StatefulSets
│   ├── daemonset.yaml           # DaemonSet sample
│   ├── job.yaml                 # Job sample
│   └── cronjob.yaml             # CronJob sample
├── helm/                        # Helm charts for K8s deployments
│   └── ecommerce-chart/         # Main Helm chart
├── monitoring/                  # Monitoring stack configuration
│   ├── docker-compose.yml       # Docker Compose for local monitoring
│   ├── prometheus.yml           # Prometheus scrape configs
│   ├── alert.rules.yml          # AlertManager alert rules
│   └── alertmanager.yml         # AlertManager configuration
├── pom.xml                      # Parent Maven POM (multi-module project)
└── common-library/
    └── pom.xml                  # Common library module
```

---

## Microservices

### 1. **API Gateway** (`api-gateway`)
- **Purpose**: Single entry point for all client requests
- **Port**: 8087
- **Responsibilities**:
  - Route requests to appropriate microservices
  - JWT token validation
  - CORS handling
  - Security filter chain
  - Fallback responses for failed services
- **Key Dependencies**:
  - Spring Cloud Gateway
  - Spring Security
  - JJWT (JSON Web Token)
  - Spring Boot Actuator
  - Resilience4j Circuit Breaker
  - Redis (reactive caching)
- **Key Classes**:
  - `ApiGatewayApplication`: Main application class
  - `SecurityConfig`: Gateway security configuration
  - `CorsConfig`: CORS policy configuration
  - `FallbackController`: Fallback endpoint for failed routes
  - `GatewaySecurityExceptionHandler`: Custom exception handling

---

### 2. **Auth Service** (`auth-service`)
- **Purpose**: User authentication and authorization
- **Port**: 8080
- **Responsibilities**:
  - User registration and login
  - JWT token generation
  - OAuth2 resource server configuration
  - Role-based access control (RBAC)
  - User profile management
- **Database**: MySQL (user credentials, roles, permissions)
- **Key Dependencies**:
  - Spring Boot Security
  - Spring Data JPA
  - JJWT (0.12.6)
  - SpringDoc OpenAPI (Swagger/OpenAPI docs)
  - MapStruct (DTO mapping)
  - MySQL Connector
- **Events**: Publishes user-related events to Kafka
- **Features**:
  - Password encryption
  - JWT token management
  - User role hierarchy

---

### 3. **Order Service** (`order-service`)
- **Purpose**: Order processing and management
- **Port**: 8088
- **Responsibilities**:
  - Create and manage customer orders
  - Order status tracking
  - Order item management
  - Inter-service communication with other services
  - Shipping address persistence
- **Database**: MySQL
- **Key Dependencies**:
  - Spring Cloud OpenFeign (inter-service calls)
  - Spring Data JPA
  - Resilience4j Circuit Breaker
  - Kafka (event streaming)
  - Micrometer (tracing & metrics)
  - Brave/Zipkin (distributed tracing)
- **Feign Clients**:
  - `CartClient`: Communicates with Cart Service
  - `InventoryClient`: Checks product availability
  - `PaymentClient`: Initiates payment
  - `ProductClient`: Fetches product details
- **Events Consumed**:
  - Payment confirmation events
  - Inventory reservation events
- **Events Published**:
  - Order created events
  - Order paid events
  - Order cancelled events

---

### 4. **Payment Service** (`payment-service`)
- **Purpose**: Payment processing and management
- **Port**: 8086
- **Responsibilities**:
  - Process payments via Razorpay
  - Payment status tracking
  - Payment reconciliation
  - Transaction logging
- **Database**: MySQL
- **External Integration**: Razorpay (v1.4.8)
- **Key Dependencies**:
  - Razorpay Java SDK
  - Spring Cloud OpenFeign
  - Spring Kafka
  - Micrometer (tracing & metrics)
- **Events Published**:
  - Payment initiated events
  - Payment successful events
  - Payment failed events
- **Error Handling**:
  - Remote error message support in exceptions
  - Feign error decoder for service errors
  - Graceful failure propagation

---

### 5. **Product Service** (`product-service`)
- **Purpose**: Product catalog management with images and categories
- **Port**: 8089
- **Responsibilities**:
  - Product CRUD operations (Create, Read, Update, Deactivate)
  - Product category management
  - Product image upload and management
  - Product search and filtering
  - Product details retrieval
- **Database**: MySQL
- **Key Dependencies**:
  - Spring Data JPA
  - Spring Validation
  - MapStruct (DTO mapping)
  - Kafka (event publishing)
  - Micrometer (metrics)
  - Brave/Zipkin (tracing)
  - Spring Security with granular permissions

**Entities**:
- `Product`: Main product entity
  - Fields: id, name, description, price (BigDecimal), sku (unique), category, active, stockQuantity, createdAt, updatedAt
  - Relationships: ManyToOne to Category
  - Indexes: idx_product_sku, idx_product_active, idx_product_category
  - Database Table: `products`

- `Category`: Product categories
  - Fields: id, name (unique), slug (unique), description, active, createdAt, updatedAt
  - Features: URL-friendly slugs for SEO
  - Indexes: idx_category_slug, idx_category_active
  - Database Table: `categories`

- `ProductImage`: Product images/media
  - Fields: id, product_id (FK), storageKey (unique), contentType, fileSize, originalFilename, displayOrder, primaryImage
  - Relationships: ManyToOne to Product with LAZY fetch
  - Indexes: idx_product_image_product, idx_product_image_product_order
  - Features: Support for multiple images per product, primary image flag, display ordering
  - Database Table: `product_images`

**Controllers**:
- `ProductController` (endpoint: `/api/products`):
  - `GET /` - Get all products with pagination (permission: PRODUCT_READ_ALL)
    - Query params: search, category, pageable
  - `GET /{id}` - Get single product (permission: PRODUCT_READ)
  - `GET /internal/{id}` - Internal service-to-service access (permission: ROLE_INTERNAL_SERVICE)
  - `POST /` - Create product (permission: PRODUCT_CREATE)
  - `PUT /{id}` - Update product (permission: PRODUCT_UPDATE)
  - `DELETE /{id}` - Deactivate product (permission: PRODUCT_DELETE)
    - Note: Soft delete via deactivate, not hard delete

- `CategoryController` (endpoint: `/api/categories`):
  - Category CRUD operations
  - Separate endpoints for category management
  - Response: CategoryResponse DTO

- `ProductImageController` (endpoint: `/api/products/{productId}/images`):
  - Image upload/management
  - Support for multiple image formats
  - Primary image selection
  - Display order management

**DTOs**:
- `CreateProductRequest`: For creating products
- `UpdateProductRequest`: For updating products
- `CreateCategoryRequest`: For creating categories
- `UpdateCategoryRequest`: For updating categories
- `ProductImageUploadRequest`: For image uploads
- `ProductImageResponse`: Image response data
- `CategoryResponse`: Category response data
- Response: `ProductResponse` (from common-library)

**Exceptions**:
- `ProductNotFoundException`: Product not found
- `CategoryNotFoundException`: Category not found
- `DuplicateSkuException`: SKU already exists
- `ProductImageNotFoundException`: Image not found
- `InvalidProductImageException`: Invalid image format/data
- `ProductImageStorageException`: Storage error during image upload
- `GlobalExceptionHandler`: Centralized exception handling

**Events Published**:
- `ProductCreatedEvent`: When product is created
  - Includes: Product ID, name, description, price, category, SKU, images
  - Consumed by: Search Service (for indexing), Inventory Service
  
- `ProductUpdatedEvent`: When product is updated
  - Includes: Product ID, changed fields, old/new values
  - Consumed by: Search Service (index update)
  
- `ProductDeletedEvent`: When product is deactivated
  - Includes: Product ID, deactivation reason
  - Consumed by: Search Service (unindex)

**Features**:
- Product categorization with slug-based URLs
- Multiple product images with primary image support
- Image display ordering
- Granular permission-based access control (PRODUCT_READ, PRODUCT_CREATE, etc.)
- Internal service-to-service endpoint with special authentication
- Soft delete (deactivation) instead of hard delete
- Database indexes for optimal query performance
- Stock quantity tracking (integrated with inventory service)
- Price management with BigDecimal for precision

---

### 6. **Cart Service** (`cart-service`)
- **Purpose**: Shopping cart management
- **Port**: 8090
- **Responsibilities**:
  - Add/remove items from cart
  - Cart persistence
  - Cart checkout
  - Cart abandonment tracking
- **Database**: MySQL
- **Key Dependencies**:
  - Spring Data JPA
  - Spring Cloud OpenFeign (product calls)
  - Resilience4j Circuit Breaker
  - Kafka (event streaming)
  - MapStruct (DTO mapping)
- **Feign Clients**:
  - `ProductClient`: Fetch product details and pricing
- **Events Published**:
  - Cart checked out events
  - Cart abandoned events
- **Circuit Breaker**: Protects calls to Product Service

---

### 7. **Inventory Service** (`inventory-service`)
- **Purpose**: Inventory and stock management
- **Port**: 8091
- **Responsibilities**:
  - Track product stock levels
  - Reserve inventory for orders
  - Release inventory on cancellation
  - Stock adjustments
- **Database**: MySQL
- **Key Dependencies**:
  - Spring Data JPA
  - Spring Cloud OpenFeign
  - Resilience4j Circuit Breaker
  - Kafka (event streaming)
  - Spring Security
- **Events Consumed**:
  - Order created events (reserve inventory)
  - Order cancelled events (release inventory)
  - Product updated events
- **Events Published**:
  - Inventory reserved events
  - Inventory released events
  - Stock updated events
- **Error Handling**:
  - `InventoryReservationException`
  - `InventoryReleaseException`
  - `InventoryConfirmException`

---

### 8. **Notification Service** (`notification-service`)
- **Purpose**: Send email and SMS notifications
- **Port**: 8092
- **Responsibilities**:
  - Send order confirmation emails
  - Send payment notifications
  - Send shipping updates
  - Send review notifications
  - Notification templating
- **Database**: MySQL (notification logs)
- **Key Dependencies**:
  - Spring Kafka
  - Spring Boot Validation
  - Micrometer (metrics)
  - Brave/Zipkin (tracing)
- **Events Consumed**:
  - Order events
  - Payment events
  - Shipping events
  - Review created events
- **Features**:
  - Asynchronous notification processing
  - Email template management
  - Notification retry logic
  - Multi-channel support (email/SMS)

---

### 9. **Shipping Service** (`shipping-service`)
- **Purpose**: Shipping and delivery management
- **Port**: 8093
- **Responsibilities**:
  - Create shipments
  - Track shipments
  - Calculate shipping costs
  - Generate tracking numbers
  - Persist shipping address snapshots
- **Database**: MySQL
- **Key Dependencies**:
  - Spring Data JPA
  - Spring Cloud OpenFeign
  - Kafka (event streaming)
  - Micrometer (tracing & metrics)
  - Brave/Zipkin (distributed tracing)
- **Feign Clients**: Inter-service communication
- **Events Consumed**:
  - Order paid events (create shipment)
- **Events Published**:
  - Shipment created events
  - Shipment status updated events
- **Features**:
  - Address snapshot persistence
  - Shipping status tracking
  - Delivery estimation

---

### 10. **Search Service** (`search-service`)
- **Purpose**: Elasticsearch-powered product and review search with advanced filtering
- **Port**: 8094
- **Responsibilities**:
  - Full-text product search with advanced filters
  - Product and review indexing
  - Search result ranking and sorting
  - Price range filtering
  - Category-based filtering
  - Real-time index updates via Kafka
- **Search Engine**: Elasticsearch (distributed, scalable, real-time)
- **Key Dependencies**:
  - Spring Boot Starter Data Elasticsearch
  - Spring Kafka (for event consumption)
  - Validation (for request validation)
  - Actuator (metrics and health)
  - Lombok (boilerplate reduction)
  - Spring Security (role-based access)

**Elasticsearch Documents**:

1. **ProductDocument** (Index: `products`):
   - Fields:
     - `productId` (Long, ID): Unique product identifier
     - `name` (Text + Keyword): Product name (multi-field for both full-text and exact match)
     - `sku` (Keyword): Stock keeping unit (exact match)
     - `category` (Keyword): Product category (faceted search)
     - `price` (Double): Product price
     - `active` (Boolean): Product availability flag
     - `primaryImageId` (Long): Primary image reference
     - `primaryImageUrl` (Keyword): Primary image URL for search results
     - `ratingSum` (Double): Sum of all ratings (for average calculation)
     - `reviewCount` (Long): Total number of reviews
     - `averageRating` (Double): Pre-calculated average rating
     - `processedReviewEventIds` (Keyword, List): Track processed review events for idempotency
   - Index Type: Multi-field index for optimized search and aggregation
   - Sharding: 5 shards (default) for scalability
   - Replicas: 1 replica per shard for high availability

2. **ReviewSearchDocument** (Index: `reviews`):
   - Fields:
     - `reviewId` (Long, ID): Unique review identifier
     - `productId` (Long): Product being reviewed
     - `userId` (Long): User who wrote review
     - `rating` (Integer): Review rating (1-5)
     - `title` (Text): Review title
     - `comment` (Text): Review content/comment
   - Features: Enables review filtering by product and rating aggregation

**Controllers**:

1. **ProductSearchController** (endpoint: `/api/search/products`):
   - `GET /` - Advanced product search
     - Query Parameters:
       - `q` (optional): Search query (searches in name and description)
       - `category` (optional): Filter by category
       - `minPrice` (optional, BigDecimal): Minimum price filter
       - `maxPrice` (optional, BigDecimal): Maximum price filter
       - `active` (optional, Boolean): Filter active/inactive products
       - `pageable`: Pagination (page, size, sort)
     - Permission: `@PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")`
       - Note: Custom role security bean checks multiple roles
       - Allows ADMIN and CUSTOMER roles to search
     - Response: `Page<ProductSearchResponse>` with pagination metadata

2. **ReviewSearchController** (endpoint: `/api/search/reviews`):
   - `GET /product/{productId}` - Get reviews for specific product
     - Path Parameter: `productId` (Long)
     - Query Parameter: `pageable` for pagination
     - Response: `Page<ReviewSearchDocument>` with sorted reviews

**Services**:

1. **ProductSearchService**:
   - `searchProducts(q, category, minPrice, maxPrice, active, pageable)`: Main search method
     - Builds complex Elasticsearch query with multiple filters
     - Applies price range filtering (minPrice AND maxPrice)
     - Category exact match filtering
     - Active status filtering
     - Full-text search on product name
     - Paginates results
     - Returns Page<ProductDocument>

2. **ProductIndexingService**:
   - `indexProduct(ProductDocument)`: Index new/updated product
   - `unindexProduct(productId)`: Remove product from index
   - Handles index updates from Kafka events
   - Manages document versioning

3. **ProductSearchSortService**:
   - Dedicated service for result sorting
   - Sort strategies:
     - By relevance (default)
     - By price (ascending/descending)
     - By rating (highest first)
     - By newest (creation date)
     - By popularity (review count)

4. **ReviewIndexingService**:
   - `indexReview(ReviewSearchDocument)`: Index review
   - `getReviewsByProductId(productId, pageable)`: Retrieve reviews for product
   - `updateProductRating(productId)`: Recalculate average rating
   - Aggregates review data for product documents

**Repositories** (Spring Data Elasticsearch):

1. **ProductSearchRepository** (extends `ElasticsearchRepository<ProductDocument, Long>`):
   - Basic CRUD operations on ProductDocument
   - Auto-generated queries for standard operations

2. **ProductSearchRepositoryCustom** (interface):
   - Custom method signatures for complex queries
   - Interface-based approach for flexibility

3. **ProductSearchRepositoryImpl** (implements ProductSearchRepositoryCustom):
   - Custom implementation of complex search queries
   - Builds Elasticsearch query DSL (Domain Specific Language)
   - Implements filter combinations
   - Handles result scoring and ranking
   - Manages pagination

4. **ReviewSearchRepository** (extends `ElasticsearchRepository<ReviewSearchDocument, Long>`):
   - Review-specific search operations
   - Queries reviews by product ID

**Kafka Consumers**:

1. **ProductCreatedConsumer**:
   - Listens to: `product-events` topic (event type: PRODUCT_CREATED)
   - Action: 
     - Extracts product details from event
     - Creates ProductDocument
     - Indexes in Elasticsearch
     - Stores processed event ID for idempotency
   - Prevents duplicate indexing via event ID tracking

2. **ReviewEventConsumer**:
   - Listens to: `review-events` topic
   - Actions:
     - `ReviewCreatedEvent`: Index new review, update product rating
     - `ReviewUpdatedEvent`: Update indexed review, recalculate rating
     - `ReviewDeletedEvent`: Remove from index, update rating
   - Triggers rating aggregation updates in ProductDocument
   - Updates `ratingSum`, `reviewCount`, `averageRating` fields

**Exceptions**:
- `InvalidSearchParameterException`: Invalid search parameters (e.g., minPrice > maxPrice)
- `GlobalExceptionHandler`: Centralized exception handling

**Security**:
- `SecurityConfig`: Spring Security configuration
- Custom role security bean: `@roleSecurity.hasAnyRole()`
- Permission-based access: Only ADMIN and CUSTOMER can search
- Method-level security with `@PreAuthorize` annotations

**Advanced Search Features**:

1. **Multi-Field Search**:
   - Product name indexed as both Text (for fuzzy matching) and Keyword (for exact match)
   - Enables "match" and "term" queries

2. **Price Range Filtering**:
   - Range queries: `minPrice <= price <= maxPrice`
   - Efficient filtering with numeric range

3. **Category Faceting**:
   - Keyword field for aggregations
   - Enables "filter by category" feature
   - Aggregations count products per category

4. **Rating-Based Sorting**:
   - Average rating calculated and stored
   - Enables sort by rating
   - Popular products appear higher

5. **Idempotent Event Processing**:
   - Tracks `processedReviewEventIds` in ProductDocument
   - Prevents duplicate reviews affecting ratings
   - Ensures consistency across retries

6. **Real-Time Updates**:
   - Kafka-driven indexing
   - Updates visible within 1 second (Elasticsearch refresh interval)
   - Eventual consistency model

**Performance Optimizations**:
- Sharding: Distributes data across multiple shards for parallel search
- Replication: Ensures high availability and read scaling
- Indexing: Background indexing of new products doesn't block writes
- Query Caching: Elasticsearch caches frequent queries
- Aggregations: Pre-computed average ratings stored in index

**Events Consumed**:
- `ProductCreatedEvent`: From Product Service → index product
- `ProductUpdatedEvent`: From Product Service → update index
- `ProductDeletedEvent`: From Product Service → unindex product
- `ReviewCreatedEvent`: From Reviews Service → index review + update product rating
- `ReviewUpdatedEvent`: From Reviews Service → update review + recalculate rating
- `ReviewDeletedEvent`: From Reviews Service → unindex review + recalculate rating

**Example Search Query**:
```
GET /api/search/products?q=laptop&category=Electronics&minPrice=40000&maxPrice=100000&active=true&page=0&size=20&sort=averageRating,desc
```
- Searches for "laptop" in name
- Filters by Electronics category
- Price range: 40,000 - 100,000
- Only active products
- Returns page 1 with 20 results
- Sorted by average rating (highest first)

---

### 11. **Reviews Service** (`reviews-service`)
- **Purpose**: Product reviews and ratings
- **Port**: 8095
- **Responsibilities**:
  - Create and manage product reviews
  - Rating aggregation
  - Review moderation
  - Review helpfulness tracking
- **Database**: MySQL
- **Key Dependencies**:
  - Spring Data JPA
  - Spring Kafka
  - Spring Cloud OpenFeign
  - Lombok
- **Feign Clients**: OpenFeign core for inter-service communication
- **Events Consumed**:
  - Order delivered events (trigger review prompts)
- **Events Published**:
  - Review created events
  - Review updated events
  - Rating changed events
- **Features**:
  - Review validation
  - Rating aggregation
  - Review filtering and sorting

---

### 12. **Eureka Server** (`eureka-server`)
- **Purpose**: Service registry and discovery
- **Port**: 8761
- **Responsibilities**:
  - Register all microservices
  - Provide service discovery
  - Health check monitoring
  - Service metadata management
- **Key Dependencies**:
  - Spring Cloud Netflix Eureka Server
  - Spring Boot Web
- **Configuration**:
  - Peer awareness
  - Instance registry
  - Lease management
- **All Services Register**: Every microservice registers with Eureka on startup

---

### 13. **Common Library** (`common-library`)
- **Purpose**: Shared code, DTOs, exceptions, and events
- **Type**: JAR (not a standalone service)
- **Components**:

#### DTOs (Data Transfer Objects):
- `CartResponse`, `CartItemResponse`: Cart data transfer
- `OrderResponse`, `OrderItemResponse`: Order data transfer
- `PaymentResponse`, `PaymentCheckoutResponse`: Payment data transfer
- `ProductResponse`: Product information
- `InventoryResponse`, `InventoryQuantityRequest`: Inventory data
- `ErrorResponse`: Standardized error responses
- `ShippingResponse`: Shipping information

#### Events (Event-Driven Architecture):
- `DomainEvent`: Base event class
- `OrderCreatedEvent`, `OrderPaidEvent`, `OrderCancelledEvent`: Order events
- `PaymentInitiatedEvent`, `PaymentSuccessEvent`, `PaymentFailedEvent`: Payment events
- `CartCheckedOutEvent`, `CartAbandonedEvent`: Cart events
- `InventoryReservedEvent`, `InventoryReleasedEvent`: Inventory events
- `ProductCreatedEvent`, `ProductUpdatedEvent`: Product events
- `ShipmentCreatedEvent`: Shipping events
- `ReviewCreatedEvent`, `ReviewUpdatedEvent`: Review events

#### Exceptions:
- `EventAlreadyProcessedException`: Idempotency handling
- `InvalidEventException`, `InvalidEventIdException`: Event validation
- `MissingEventIdException`, `MissingEventTypeException`: Event integrity
- `InventoryConfirmException`, `InventoryReleaseException`, `InventoryReservationException`: Inventory errors
- `MissingCustomerIdException`: Customer validation
- `ServiceUnavailableException`: Remote service failures

#### Kafka Infrastructure:
- `KafkaTopics`: Topic name constants
- `EventType`: Event type enumeration
- `CommonKafkaRetryConfig`: Retry configuration
- `OutboxEvent`, `OutboxProducer`: Transactional outbox pattern
- `OutboxScheduler`: Scheduled outbox processor
- `EventIdempotencyService`: Prevents duplicate event processing
- `ProcessedEvent`, `ProcessedEventRepository`: Tracks processed events
- `OutboxRepository`: Manages outbox entries

#### Security:
- `CurrentUser`: Custom annotation for extracting current user
- `GatewayAuthenticationFilter`: JWT validation filter for gateway
- `InternalServiceAuthenticationFilter`: Service-to-service auth
- `JwtClaimsExtractor`: JWT claim extraction
- `GatewaySecurityConfiguration`: Gateway security setup
- `FeignSecurityConfiguration`: Feign client security setup
- `FeignSecurityInterceptor`: Adds JWT to Feign requests
- `FeignErrorDecoder`: Handles Feign errors gracefully
- `GatewaySecurityHeaders`: Custom security headers
- `GatewayUserPrincipal`: User principal object

#### Enums:
- `Role`: User roles (ADMIN, USER, MANAGER, etc.)
- `PaymentMethod`: Payment methods (CREDIT_CARD, DEBIT_CARD, etc.)
- `PaymentStatus`: Payment states (PENDING, SUCCESS, FAILED, REFUNDED)
- `OrderStatus`: Order states (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- `Currency`: Supported currencies

#### Base Classes:
- `BaseEntity`: Common JPA entity fields (ID, timestamps)

#### Configuration:
- `PaginationConfig`, `PaginationProperties`: Pagination settings
- Centralized configuration for all services

---

## Technology Stack

### Core Framework
- **Java 21**: Latest LTS version for performance and features
- **Spring Boot 3.3.12**: Latest stable version
- **Spring Cloud 2023.0.3**: Microservices patterns and tools

### Service Communication
- **Spring Cloud Netflix Eureka**: Service registry and discovery
- **Spring Cloud Gateway**: API gateway and routing
- **Spring Cloud OpenFeign**: Declarative HTTP client
- **Spring Cloud Circuit Breaker (Resilience4j)**: Fault tolerance

### Data & Persistence
- **Spring Data JPA**: ORM and data access
- **MySQL 8.x**: Relational database
- **H2 Database**: In-memory testing database

### Messaging & Events
- **Apache Kafka**: Event streaming and asynchronous communication
- **Spring Kafka**: Kafka integration with Spring

### Search
- **Elasticsearch**: Full-text search engine
- **Spring Data Elasticsearch**: Spring integration

### Payment Processing
- **Razorpay Java SDK (1.4.8)**: Payment gateway integration

### Monitoring & Observability
- **Micrometer**: Application metrics facade
- **Prometheus**: Metrics collection and storage
- **Grafana**: Metrics visualization and dashboards
- **Brave/Zipkin**: Distributed request tracing
- **Spring Boot Actuator**: Health checks and metrics endpoints

### Security
- **Spring Security**: Authentication and authorization
- **JJWT (0.12.5-0.12.6)**: JWT token creation and validation
- **OAuth2 Resource Server**: Token-based security

### Development Tools
- **Lombok**: Boilerplate code reduction (annotations)
- **MapStruct (1.6.3)**: DTO and entity mapping
- **Spring Boot DevTools**: Development productivity

### Code Quality
- **SonarQube Maven Plugin**: Code quality analysis

### Build & Deployment
- **Maven**: Build automation
- **Spring Boot Maven Plugin**: Package as executable JAR
- **Docker**: Containerization
- **Kubernetes**: Orchestration and deployment
- **Helm**: Package manager for Kubernetes

---

## Architecture

### High-Level Architecture

```
┌─────────────┐
│   Clients   │  (Web, Mobile, Desktop)
└──────┬──────┘
       │
       ▼
┌──────────────────────────────┐
│   API Gateway (8087)         │  ◄─── Spring Cloud Gateway
│  - Route Requests            │  ◄─── JWT Validation
│  - Security Filter           │  ◄─── CORS Handling
└──────┬───────────────────────┘
       │
       ├─────────┬──────────┬──────────┬────────────┬─────────────┐
       │         │          │          │            │             │
       ▼         ▼          ▼          ▼            ▼             ▼
    ┌────┐  ┌────────┐  ┌──────┐  ┌────────┐  ┌──────────┐  ┌────────┐
    │Auth│  │Product │  │Order │  │Payment │  │Inventory │  │Shipping│
    │Svc │  │Svc     │  │Svc   │  │Svc     │  │Svc       │  │Svc     │
    └────┘  └────────┘  └──────┘  └────────┘  └──────────┘  └────────┘
    
    ┌────┐  ┌────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐
    │Cart│  │Search  │  │Reviews   │  │Notif     │  │Eureka   │
    │Svc │  │Svc     │  │Svc       │  │Svc       │  │Server   │
    └────┘  └────────┘  └──────────┘  └──────────┘  └─────────┘
       │         │          │          │            │
       └─────────┴──────────┴──────────┴────────────┴─────────────┐
                                                                   │
                         ┌─────────────────────────────────────────┘
                         │
                         ▼
                    ┌──────────────┐
                    │ Kafka Topics │  ◄─── Event Streaming
                    │ (Events)     │  ◄─── Async Communication
                    └──────────────┘
                    
                    ┌──────────────┐
                    │ MySQL        │  ◄─── Data Persistence
                    │ Database     │
                    └──────────────┘
                    
                    ┌──────────────┐
                    │ Elasticsearch│  ◄─── Full-Text Search
                    └──────────────┘
```

### Communication Patterns

1. **Synchronous** (REST):
   - API Gateway to Services
   - Service-to-Service via OpenFeign
   - Circuit breaker protection

2. **Asynchronous** (Kafka):
   - Event publishing on database changes
   - Cross-service notifications
   - Event idempotency handled

3. **Service Discovery**:
   - All services register with Eureka
   - Gateway discovers services dynamically
   - Health checks via Eureka

### Event Flow

```
Service A (creates order)
    │
    ├─► Save to Database
    │
    ├─► Publish Event to Kafka Topic
    │
    ▼
Kafka Topic
    │
    ├─► Service B (Inventory) - consumes
    ├─► Service C (Payment) - consumes
    ├─► Service D (Notification) - consumes
    └─► Service E (Shipping) - consumes
```

---

## Getting Started

### Prerequisites

1. **Java 21** or later
2. **Maven 3.8+**
3. **Docker & Docker Compose**
4. **MySQL 8.x**
5. **Kafka & Zookeeper** (or use Docker Compose)
6. **Elasticsearch** (optional, for search service)

### Local Development Setup

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd ecommerce-microservices
```

#### 2. Build the Project
```bash
# Build all modules
mvn clean install

# Build specific service
mvn clean install -pl order-service
```

#### 3. Start Infrastructure (Docker Compose)

From the `monitoring/` directory:
```bash
docker-compose up -d

# This starts:
# - Zipkin (port 9411) - distributed tracing
# - Prometheus (port 9090) - metrics
# - AlertManager (port 9093) - alerting
# - Grafana (port 3000) - dashboards
```

#### 4. Run Individual Services

Each service has a main application class. For development:

```bash
# Terminal 1 - Eureka Server (must start first)
mvn spring-boot:run -pl eureka-server

# Terminal 2 - API Gateway
mvn spring-boot:run -pl api-gateway

# Terminal 3 - Auth Service
mvn spring-boot:run -pl auth-service

# Terminal 4 - Order Service
mvn spring-boot:run -pl order-service

# ... and so on for other services
```

#### 5. Verify Services are Running

Check Eureka Dashboard:
```
http://localhost:8761
```

All registered services should appear in the dashboard.

#### 6. Test API

```bash
# Health check
curl http://localhost:8087/actuator/health

# Get metrics
curl http://localhost:8087/actuator/prometheus
```

---

## Service Configuration

### Application Properties Pattern

Each service has minimal `application.properties`:
```properties
spring.application.name=service-name
```

### Database Configuration

Each service connects to MySQL with Spring Data JPA:
- Database: `service_database` (per service)
- Username: configured via Kubernetes Secrets
- Password: configured via Kubernetes Secrets

### Kafka Configuration

Topic names defined in `common-library/kafka/KafkaTopics.java`:
- `order-events`: Order-related events
- `payment-events`: Payment-related events
- `inventory-events`: Inventory-related events
- `product-events`: Product catalog events
- `cart-events`: Shopping cart events
- `shipping-events`: Shipping-related events
- `review-events`: Review and rating events
- `notification-events`: Notification delivery events

### Resilience Configuration

Services use Resilience4j with common defaults:
- **Timeout**: 5 seconds
- **Retry Attempts**: 3
- **Circuit Breaker Threshold**: 50% failure rate
- **Wait Duration**: 1 minute

---

## Deployment

### Docker Build

```bash
# Build Docker image for each service
docker build -f order-service/Dockerfile -t ecommerce/order-service:1.0 order-service/

# Or use Maven plugin
mvn spring-boot:build-image -pl order-service
```

### Kubernetes Deployment

#### 1. Create Namespace
```bash
kubectl apply -f kubernetes/namespace/namespace.yaml
```

#### 2. Create ConfigMaps and Secrets
```bash
kubectl apply -f kubernetes/common/configmap.yaml
kubectl apply -f kubernetes/common/secret.yaml
```

#### 3. Deploy Infrastructure
```bash
# MySQL
kubectl apply -f kubernetes/mysql/

# Kafka & Zookeeper
kubectl apply -f kubernetes/kafka/

# Eureka Server
kubectl apply -f kubernetes/eureka/
```

#### 4. Deploy Microservices
```bash
# Apply all service deployments
kubectl apply -f kubernetes/auth-service/
kubectl apply -f kubernetes/product-service/
kubectl apply -f kubernetes/order-service/
kubectl apply -f kubernetes/payment-service/
# ... etc
```

#### 5. Deploy API Gateway (Last)
```bash
kubectl apply -f kubernetes/api-gateway/
```

#### 6. Deploy Monitoring Stack
```bash
kubectl apply -f kubernetes/monitoring/
```

### Helm Deployment

```bash
# Install using Helm chart
helm install ecommerce helm/ecommerce-chart/ \
  --namespace ecommerce \
  --values helm/ecommerce-chart/values.yaml

# Upgrade deployment
helm upgrade ecommerce helm/ecommerce-chart/ \
  --namespace ecommerce

# Uninstall
helm uninstall ecommerce --namespace ecommerce
```

### Kubernetes YAML Components

#### Deployments (49 YAML files total):
- `deployment.yaml`: Nginx example with probes (liveness, readiness, startup)
- Service-specific deployments for each microservice
- Auto-scaling configuration

#### StatefulSets:
- `kubernetes/mysql/statefulset.yaml`: Persistent MySQL database
- `kubernetes/kafka/kafka-statefulset.yaml`: Kafka brokers
- `kubernetes/kafka/zookeeper-statefulset.yaml`: Zookeeper ensemble

#### Services:
- `kubernetes/*/service.yaml`: ClusterIP services for each microservice
- `kubernetes/headless-service.yaml`: Headless service for StatefulSets
- `kubernetes/mysql/service.yaml`: MySQL service

#### ConfigMaps & Secrets:
- `kubernetes/common/configmap.yaml`: Application configuration
- `kubernetes/common/secret.yaml`: Sensitive data (passwords, keys)
- `kubernetes/mysql/secret.yaml`: Database credentials
- Service-specific configs and secrets

#### Storage:
- `kubernetes/common/persistent-volume.yaml`: Manual PV provisioning
- `kubernetes/common/persistent-volume-claim.yaml`: PVC claiming storage
- `kubernetes/pvc-dynamic.yaml`: Dynamic provisioning with storage classes

#### Advanced Workloads:
- `kubernetes/daemonset.yaml`: DaemonSet example (runs on every node)
- `kubernetes/job.yaml`: One-time job (e.g., data migration)
- `kubernetes/cronjob.yaml`: Scheduled job (e.g., cleanup tasks)

#### Monitoring:
- `kubernetes/monitoring/prometheus.yaml`: Prometheus deployment
- `kubernetes/monitoring/grafana.yaml`: Grafana deployment
- `kubernetes/monitoring/alertmanager.yaml`: AlertManager deployment

#### Pod Configuration:
- `kubernetes/pod.yaml`: Basic pod definition example
- Includes resource requests/limits
- Health probes (liveness, readiness, startup)

---

## Monitoring & Observability

### Prometheus Metrics

**Scrape Interval**: 10 seconds (configured in `prometheus.yml`)

**Monitored Services**:
- API Gateway (port 8087) - `/actuator/prometheus`
- Order Service (port 8088) - `/actuator/prometheus`
- Product Service (port 8089) - `/actuator/prometheus`
- Payment Service (port 8086) - `/actuator/prometheus`

**Metrics Include**:
- HTTP request count/duration
- JVM heap usage
- Database connection pool stats
- Kafka producer/consumer metrics
- Circuit breaker state
- Thread counts

### Grafana Dashboards

**Access**: http://localhost:3000 (default: admin/admin)

**Pre-configured Dashboards**:
- Application metrics
- JVM metrics
- HTTP traffic
- Database performance
- Kafka topics and partitions

### Distributed Tracing

**Zipkin**: http://localhost:9411

**Features**:
- Trace service calls across all services
- View request flow end-to-end
- Identify latency bottlenecks
- Debug complex inter-service calls

**Integration**:
- All services send traces via Brave
- Trace IDs propagated through requests
- Spring Cloud Sleuth provides correlation

### Alert Manager

**Port**: 9093

**Alert Rules** (`alert.rules.yml`):
- High error rates
- Service downtime
- Resource exhaustion
- Circuit breaker trips

**Alert Destinations**:
- Email (configurable)
- Slack (webhook)
- Custom webhooks

### Health Checks

**Spring Boot Actuator Endpoints**:
```bash
# Health status
curl http://localhost:8087/actuator/health

# Detailed health
curl http://localhost:8087/actuator/health/details

# Service info
curl http://localhost:8087/actuator/info

# Metrics list
curl http://localhost:8087/actuator/metrics
```

---

## Database

### MySQL Configuration

**Version**: 8.x

**Per-Service Databases**:
- `auth_service_db`: Auth Service
- `order_service_db`: Order Service
- `product_service_db`: Product Service
- `payment_service_db`: Payment Service
- `cart_service_db`: Cart Service
- `inventory_service_db`: Inventory Service
- `notification_service_db`: Notification Service
- `shipping_service_db`: Shipping Service
- `reviews_service_db`: Reviews Service

### Spring Data JPA

**Features**:
- Automatic table creation/migration
- Entity relationships (OneToMany, ManyToOne, etc.)
- Query methods
- Transaction management
- Audit fields via `BaseEntity`

### Common Base Entity

All entities extend `BaseEntity` from common-library:
```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    private Long id;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Database Transactions

- Service layers manage transactions
- Event publishing within transaction
- Rollback handling with error propagation

---

## Message Queue

### Apache Kafka

**Deployment**:
- Kubernetes StatefulSet (3 brokers recommended)
- Zookeeper for coordination
- Persistent volumes for data

**Topics** (managed by KafkaTopics enum):

| Topic | Purpose | Partitions |
|-------|---------|-----------|
| order-events | Order lifecycle events | 3 |
| payment-events | Payment processing | 3 |
| inventory-events | Stock management | 3 |
| product-events | Product catalog | 3 |
| cart-events | Shopping cart | 3 |
| shipping-events | Delivery tracking | 3 |
| review-events | Product reviews | 3 |
| notification-events | Notifications | 3 |

### Event Publishing

**Transactional Outbox Pattern**:
1. Service saves entity to database
2. Event saved to `outbox` table in same transaction
3. Scheduled outbox processor publishes events
4. Guarantees at-least-once delivery
5. Idempotency prevents duplicate processing

### Event Consumption

**Idempotency**:
- `EventIdempotencyService` tracks processed events
- Prevents duplicate message processing
- Uses `ProcessedEvent` table

**Error Handling**:
- Dead-letter topics for failed events
- Retry configuration per topic
- Exponential backoff

---

## API Gateway

### Purpose
Central entry point for all client requests with:
- Request routing
- Authentication/Authorization
- CORS handling
- Rate limiting
- Load balancing

### Configuration

**Routes** (in `application.yml` typically):
- `/api/auth/**` → auth-service:8080
- `/api/products/**` → product-service:8089
- `/api/orders/**` → order-service:8088
- `/api/payments/**` → payment-service:8086
- `/api/cart/**` → cart-service:8090
- `/api/inventory/**` → inventory-service:8091
- `/api/shipping/**` → shipping-service:8093
- `/api/search/**` → search-service:8094
- `/api/reviews/**` → reviews-service:8095
- `/api/notifications/**` → notification-service:8092

### Security

**JWT Validation**:
- All requests validated via `GatewayAuthenticationFilter`
- Token extracted from Authorization header
- Claims verified using signing key

**CORS Policy** (CorsConfig):
```
Allowed Origins: http://localhost:3000, http://localhost:4200, etc.
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Content-Type, Authorization, etc.
Max Age: 3600 seconds
```

### Circuit Breaker

**Resilience4j Integration**:
- Fallback responses when services are down
- `FallbackController` handles failures
- Service recovery detection

---

## Inter-Service Communication

### OpenFeign Clients

**Order Service Clients**:
```java
@FeignClient(name = "cart-service")
public interface CartClient { ... }

@FeignClient(name = "inventory-service")
public interface InventoryClient { ... }

@FeignClient(name = "payment-service")
public interface PaymentClient { ... }

@FeignClient(name = "product-service")
public interface ProductClient { ... }
```

### Error Handling

**FeignErrorDecoder** (`common-library`):
- Converts HTTP errors to domain exceptions
- Supports `ServiceUnavailableException`
- Propagates remote error details
- Graceful degradation

### Request/Response Flow

1. Client calls API Gateway
2. Gateway routes to service A
3. Service A needs data from Service B
4. Service A uses Feign client to call Service B
5. Service B responds
6. Service A processes response
7. Response returned to Gateway
8. Gateway returns to client

---

## Security

### Authentication

**JWT (JSON Web Tokens)**:
- Issued by Auth Service
- Contains user ID, roles, permissions
- Validated by Gateway and services
- JJWT (0.12.5-0.12.6) for token handling

**Token Format**:
```
Header.Payload.Signature

Payload:
{
  "sub": "user123",
  "name": "John Doe",
  "roles": ["USER", "CUSTOMER"],
  "exp": 1234567890
}
```

### Authorization

**Role-Based Access Control (RBAC)**:
- `@EnableMethodSecurity` for method-level security
- `@PreAuthorize` for role checks
- `@Secured` for method-level security

**Roles** (from common-library):
- ADMIN: Full system access
- USER: Standard user access
- CUSTOMER: Customer-specific access
- MANAGER: Management features

### Service-to-Service Security

**Internal Service Auth**:
- `InternalServiceAuthenticationFilter` validates service tokens
- `FeignSecurityInterceptor` adds JWT to outbound Feign requests
- Services authenticated via special service account tokens

### Encryption

**Password Storage**:
- Bcrypt hashing via Spring Security
- Salt generation automatic

**Sensitive Data**:
- Kubernetes Secrets for passwords/API keys
- Environment variables in containers
- No hardcoded credentials

---

## Payment Gateway Integration

### Razorpay Overview

**Integration Method**: Razorpay Java SDK (v1.4.8)

**Location**: Payment Service (`payment-service`)

**Purpose**: 
- Process credit/debit card payments
- Handle payment authorization and settlement
- Support multiple payment methods
- Provide secure payment processing

### Payment Service Architecture

**Payment Flow**:
```
1. Client initiates payment
   └─► API Gateway routes to Payment Service

2. Payment Service receives payment request
   └─► Validates payment amount and customer

3. Payment Service calls Razorpay API
   └─► Creates payment order with Razorpay
   └─► Returns payment order details to client

4. Client completes payment on Razorpay hosted UI
   └─► Razorpay processes payment
   └─► Razorpay sends webhook notification back to our service

5. Webhook Handler receives payment confirmation
   └─► Verifies webhook signature
   └─► Updates payment status in database
   └─► Publishes payment success event to Kafka
   └─► Order Service consumes event and updates order status
```

### Razorpay SDK Setup

**Dependency** (in `payment-service/pom.xml`):
```xml
<dependency>
    <groupId>com.razorpay</groupId>
    <artifactId>razorpay-java</artifactId>
    <version>1.4.8</version>
</dependency>
```

**Razorpay Credentials** (stored in Kubernetes Secrets):
- `RAZORPAY_KEY_ID`: Public key for client-side
- `RAZORPAY_KEY_SECRET`: Secret key for server-side verification

**Configuration** (in `application.yml`):
```yaml
razorpay:
  key-id: ${RAZORPAY_KEY_ID}
  key-secret: ${RAZORPAY_KEY_SECRET}
  webhook-secret: ${RAZORPAY_WEBHOOK_SECRET}
  timeout: 30000  # milliseconds
```

---

## Cloudflare Tunnel Setup

### Purpose of Cloudflare Tunnel

Cloudflare Tunnel (formerly Argo Tunnel) is used to:
- Expose the Payment Service webhook endpoint publicly
- Create a secure tunnel from your local/private network to Cloudflare's edge network
- Receive Razorpay webhook callbacks without opening ports on your firewall
- Provide HTTPS with automatic certificate management

### Architecture with Cloudflare Tunnel

```
Local Network (Private)
    │
    ├─► Payment Service (port 8086)
    │       │
    │       ├─► /api/payments/webhook/razorpay
    │       │       (receives webhook POST requests)
    │       │
    │       └─► Cloudflared Daemon
    │               │
    └───────────────┘
                    │
                    ▼
            Cloudflare Edge Network
                    │
                    ├─► Global CDN
                    ├─► DDoS Protection
                    ├─► TLS/SSL
                    └─► DNS Routing
                    
                    │
                    ▼
            Razorpay Servers
                    │
                    ├─► Payment Processing
                    ├─► Webhook Service
                    └─► Sends callback to tunnel URL
```

### Installation & Setup

#### 1. Install Cloudflared

**On Linux**:
```bash
# Add Cloudflare repository
curl -L --output cloudflared.deb https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
sudo dpkg -i cloudflared.deb

# Verify installation
cloudflared --version
```

**On macOS**:
```bash
brew install cloudflare/cloudflare/cloudflared
cloudflared --version
```

**On Windows**:
```bash
# Download from GitHub
https://github.com/cloudflare/cloudflared/releases/latest

# Extract and add to PATH
# Or use: choco install cloudflared
```

#### 2. Authenticate with Cloudflare

```bash
# Login to Cloudflare account
cloudflared tunnel login

# This will:
# - Open browser to Cloudflare dashboard
# - Ask to select your domain
# - Generate authentication certificate
# - Save cert.pem to ~/.cloudflared/
```

#### 3. Create Tunnel Configuration

**Create config file** (`~/.cloudflared/config.yml`):
```yaml
tunnel: ecommerce-payment-webhook
credentials-file: /home/user/.cloudflared/ecommerce-payment-webhook.json

ingress:
  # Webhook endpoint - most specific routes first
  - hostname: webhook.yourdomain.com
    service: http://localhost:8086
    
  # Catch-all for other requests (optional)
  - service: http_status:404
```

**Alternative: Command Line Setup**:
```bash
# Create named tunnel
cloudflared tunnel create ecommerce-payment-webhook

# Create routing rule for webhook
cloudflared tunnel route dns ecommerce-payment-webhook webhook.yourdomain.com

# Run tunnel
cloudflared tunnel run ecommerce-payment-webhook
```

#### 4. Start Cloudflared Tunnel

```bash
# Simple start (from config)
cloudflared tunnel run ecommerce-payment-webhook

# With verbose logging
cloudflared tunnel run ecommerce-payment-webhook --loglevel debug

# In background (Linux/macOS)
nohup cloudflared tunnel run ecommerce-payment-webhook > cloudflared.log 2>&1 &

# As systemd service (Linux)
sudo cloudflared service install
sudo systemctl start cloudflared

# As Windows Service
cloudflared service install
net start cloudflared
```

#### 5. Verify Tunnel is Running

```bash
# Check tunnel status
cloudflared tunnel list

# Test connectivity
curl -H "CF-Access-Client-ID: <id>" https://webhook.yourdomain.com/actuator/health

# Check tunnel logs
cloudflared tunnel logs ecommerce-payment-webhook
```

---

## Razorpay Webhook Integration

### Webhook Endpoint

**Payment Service Webhook Controller**:
- **URL**: `https://webhook.yourdomain.com/api/payments/webhook/razorpay`
- **Method**: `POST`
- **Content-Type**: `application/json`

**Controller Implementation**:
```java
@RestController
@RequestMapping("/api/payments/webhook")
public class WebhookController {
    
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        
        try {
            // Verify webhook signature
            verifyWebhookSignature(payload, signature);
            
            // Parse webhook data
            RazorpayWebhookEvent event = parseWebhookEvent(payload);
            
            // Process based on event type
            processWebhookEvent(event);
            
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            logger.error("Webhook processing failed", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
    
    private void verifyWebhookSignature(String payload, String signature) {
        String expectedSignature = calculateSignature(payload, webhookSecret);
        if (!expectedSignature.equals(signature)) {
            throw new SecurityException("Invalid webhook signature");
        }
    }
}
```

### Webhook Events Handled

**1. Payment Authorized**:
```json
{
  "event": "payment.authorized",
  "created_at": 1234567890,
  "payload": {
    "payment": {
      "entity": "payment",
      "id": "pay_1234567890abcd",
      "amount": 50000,  // Amount in paise
      "currency": "INR",
      "status": "authorized",
      "method": "card",
      "description": "Order #12345",
      "amount_refunded": 0,
      "refund_status": null,
      "captured": false,
      "description": "Order #12345",
      "card_id": "card_1234567890",
      "bank": null,
      "wallet": null,
      "vpa": null,
      "email": "customer@example.com",
      "contact": "+919876543210"
    }
  }
}
```

**2. Payment Captured**:
```json
{
  "event": "payment.captured",
  "created_at": 1234567891,
  "payload": {
    "payment": {
      "id": "pay_1234567890abcd",
      "amount": 50000,
      "status": "captured",
      "captured": true
    }
  }
}
```

**3. Payment Failed**:
```json
{
  "event": "payment.failed",
  "created_at": 1234567892,
  "payload": {
    "payment": {
      "id": "pay_1234567890abcd",
      "amount": 50000,
      "status": "failed",
      "reason_code": "BAD_REQUEST_ERROR",
      "reason": "Payment declined by bank"
    }
  }
}
```

### Webhook Processing Flow

```
1. Receive Webhook from Razorpay
   └─► Via Cloudflare Tunnel
   └─► HTTPS encrypted
   └─► Contains X-Razorpay-Signature header

2. Verify Signature
   └─► Use HMAC-SHA256
   └─► Compare with webhook secret
   └─► Reject if invalid

3. Parse Event Payload
   └─► Extract event type
   └─► Extract payment details
   └─► Extract order ID

4. Update Payment Status
   └─► Query Payment entity from database
   └─► Update status based on event
   └─► Save to database

5. Publish Kafka Event
   └─► PaymentSuccessEvent (if payment.captured)
   └─► PaymentFailedEvent (if payment.failed)
   └─► PaymentAuthorizedEvent (if payment.authorized)

6. Order Service Consumes Event
   └─► Updates order status
   └─► Triggers inventory reservation
   └─► Initiates fulfillment process

7. Return 200 OK
   └─► Razorpay marks webhook as delivered
   └─► Prevents retry attempts
```

### Webhook Signature Verification

**Algorithm**: HMAC SHA256

**Implementation**:
```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

private String calculateSignature(String payload, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    mac.init(key);
    byte[] signature = mac.doFinal(payload.getBytes());
    return Base64.getEncoder().encodeToString(signature);
}

// Or use Razorpay SDK built-in method:
RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
boolean isValid = razorpay.verifyWebhookSignature(payload, signature, webhookSecret);
```

### Webhook Retry Configuration

Razorpay automatically retries failed webhooks:

**Default Retry Strategy**:
- **Attempt 1**: Immediate
- **Attempt 2**: After 5 minutes
- **Attempt 3**: After 30 minutes
- **Attempt 4**: After 2 hours
- **Attempt 5**: After 5 hours

**Headers Sent by Razorpay**:
- `User-Agent`: Razorpay/2.0
- `X-Razorpay-Signature`: HMAC signature
- `X-Razorpay-Event-Id`: Unique event ID (for idempotency)

### Idempotent Webhook Processing

**Prevent Duplicate Processing**:
```java
@Component
public class WebhookIdempotencyService {
    
    private final ProcessedWebhookRepository repository;
    
    public boolean isAlreadyProcessed(String webhookEventId) {
        return repository.existsById(webhookEventId);
    }
    
    public void markAsProcessed(String webhookEventId) {
        ProcessedWebhook webhook = new ProcessedWebhook();
        webhook.setEventId(webhookEventId);
        webhook.setProcessedAt(LocalDateTime.now());
        repository.save(webhook);
    }
}

// Usage in controller:
@PostMapping("/razorpay")
public ResponseEntity<String> handleRazorpayWebhook(
        @RequestBody String payload,
        @RequestHeader("X-Razorpay-Event-Id") String eventId,
        @RequestHeader("X-Razorpay-Signature") String signature) {
    
    // Check if already processed
    if (idempotencyService.isAlreadyProcessed(eventId)) {
        return ResponseEntity.ok("Already processed");
    }
    
    try {
        verifyWebhookSignature(payload, signature);
        RazorpayWebhookEvent event = parseWebhookEvent(payload);
        processWebhookEvent(event);
        idempotencyService.markAsProcessed(eventId);
        return ResponseEntity.ok("Processed");
    } catch (Exception e) {
        // Don't mark as processed on error - Razorpay will retry
        return ResponseEntity.status(500).body("Error");
    }
}
```

### Testing Webhook Locally

**1. Using Razorpay Test Mode**:
```bash
# Use Razorpay test credentials (API Key ID and Secret)
# Create test payments and trigger webhooks from dashboard
```

**2. Manual Webhook Test**:
```bash
# Create webhook request locally
curl -X POST https://webhook.yourdomain.com/api/payments/webhook/razorpay \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: <your-signature>" \
  -d '{
    "event": "payment.captured",
    "created_at": '$(date +%s)',
    "payload": {
      "payment": {
        "id": "pay_test123",
        "amount": 50000,
        "status": "captured"
      }
    }
  }'
```

**3. Webhook Testing Tools**:
```bash
# Using ngrok (alternative to Cloudflare)
ngrok http 8086

# Using webhook.site for testing
curl -X POST https://webhook.site/unique-id \
  -H "X-Razorpay-Signature: test" \
  -d '{"event":"payment.captured"}'
```

### Razorpay Dashboard Configuration

**1. Register Webhook URL**:
- Log in to Razorpay Dashboard
- Go to Settings → Webhooks
- Add new webhook URL: `https://webhook.yourdomain.com/api/payments/webhook/razorpay`
- Select events to subscribe:
  - `payment.authorized`
  - `payment.captured`
  - `payment.failed`
  - `payment.disputed`
  - `payment.refunded`

**2. Webhook Secret**:
- Razorpay generates webhook secret
- Store securely in Kubernetes Secret
- Use for signature verification

**3. Enable Webhook**:
- Activate webhook in dashboard
- Razorpay shows webhook URL status (Active/Inactive)
- Monitor webhook delivery logs

### Payment Service Kafka Events

**Published Events** (after webhook processing):

1. **PaymentSuccessEvent**:
   ```json
   {
     "orderId": "ORDER-123",
     "paymentId": "pay_1234567890",
     "amount": 50000,
     "currency": "INR",
     "status": "SUCCESS",
     "timestamp": "2024-09-23T07:27:21Z"
   }
   ```

2. **PaymentFailedEvent**:
   ```json
   {
     "orderId": "ORDER-123",
     "paymentId": "pay_1234567890",
     "amount": 50000,
     "reason": "Payment declined by bank",
     "status": "FAILED",
     "timestamp": "2024-09-23T07:27:21Z"
   }
   ```

**Consumed By**:
- Order Service → Updates order status to PAID
- Inventory Service → Confirms inventory reservation
- Notification Service → Sends payment confirmation email
- Shipping Service → Prepares shipment

### Error Handling in Webhook

**Transient Errors** (Razorpay retries):
- Network timeout
- Service unavailable (5xx)
- Other temporary failures

**Permanent Errors** (shouldn't occur):
- Invalid signature (400)
- Malformed JSON (400)
- Missing required fields (400)

**Best Practices**:
```java
@PostMapping("/razorpay")
public ResponseEntity<String> handleRazorpayWebhook(...) {
    try {
        // Verify signature first (security)
        verifyWebhookSignature(payload, signature);
        
        // Parse event
        RazorpayWebhookEvent event = parseWebhookEvent(payload);
        
        // Use transaction
        @Transactional
        void processEvent() {
            // Update payment
            // Publish Kafka event
            // Mark as processed
        }
        
        return ResponseEntity.ok("OK");
        
    } catch (SecurityException e) {
        // Invalid signature - permanent error
        logger.error("Invalid webhook signature", e);
        return ResponseEntity.status(403).body("Forbidden");
        
    } catch (JsonProcessingException e) {
        // Malformed JSON - permanent error
        logger.error("Invalid JSON in webhook", e);
        return ResponseEntity.status(400).body("Bad Request");
        
    } catch (Exception e) {
        // Transient error - Razorpay will retry
        logger.error("Error processing webhook", e);
        return ResponseEntity.status(500).body("Internal Server Error");
    }
}
```

### Monitoring Webhook Health

**Metrics to Track**:
```bash
# Webhook success rate
curl http://localhost:8086/actuator/metrics/webhooks.processed

# Webhook processing time
curl http://localhost:8086/actuator/metrics/webhooks.duration

# Webhook errors
curl http://localhost:8086/actuator/metrics/webhooks.errors
```

**Alerts to Configure** (in AlertManager):
- Webhook success rate drops below 95%
- Webhook processing time exceeds 5 seconds
- Payment status mismatch detected
- Signature verification failures

---

## Troubleshooting

### Services Not Registering with Eureka

**Issue**: Services don't appear in Eureka dashboard

**Solutions**:
```bash
# 1. Check Eureka Server is running
curl http://localhost:8761

# 2. Check service logs for connection errors
docker logs <service-container>

# 3. Verify network connectivity
docker network inspect ecommerce-network

# 4. Check service health
curl http://localhost:<port>/actuator/health
```

### Kafka Messages Not Being Consumed

**Issue**: Events published but not processed

**Solutions**:
```bash
# 1. Check Kafka is running
docker ps | grep kafka

# 2. List topics
kafka-topics.sh --list --bootstrap-server kafka:9092

# 3. Check consumer groups
kafka-consumer-groups.sh --list --bootstrap-server kafka:9092

# 4. Check for dead-letter topics
kafka-topics.sh --list --bootstrap-server kafka:9092 | grep DLT

# 5. Review service logs for consumption errors
```

### Circuit Breaker Open

**Issue**: Service calls failing with circuit breaker exception

**Symptoms**:
- Rapid 500 errors
- Service unavailable messages
- Logs show CircuitBreakerOpenException

**Solutions**:
```bash
# 1. Check target service health
curl http://target-service:port/actuator/health

# 2. Check network connectivity
docker exec <container> ping target-service

# 3. Verify circuit breaker metrics
curl http://service:port/actuator/metrics/resilience4j.circuitbreaker.state

# 4. Service typically recovers after wait-duration (check config)
```

### Database Connection Issues

**Issue**: "Cannot get a connection" or "Connection pool exhausted"

**Solutions**:
```bash
# 1. Check MySQL is running
docker ps | grep mysql

# 2. Verify database exists
mysql -u root -p -e "SHOW DATABASES;"

# 3. Check connection pool metrics
curl http://service:port/actuator/metrics/hikaricp.connections

# 4. Review application logs for connection errors
```

### Distributed Tracing Not Working

**Issue**: No traces in Zipkin dashboard

**Solutions**:
```bash
# 1. Verify Zipkin is running
curl http://localhost:9411

# 2. Check Brave configuration in services
# Verify micrometer-tracing-bridge-brave is in pom.xml

# 3. Check for trace ID in logs
# Should appear in console: [order-service,abc123def456,xyz789]

# 4. Verify network connectivity to Zipkin
docker exec <service-container> curl http://zipkin:9411
```

### High Memory Usage

**Issue**: Pod/Container memory limits exceeded

**Solutions**:
```bash
# 1. Check current usage
docker stats <container>

# 2. Review heap metrics
curl http://service:port/actuator/metrics/jvm.memory.used

# 3. Increase container limits in Kubernetes
# Edit deployment and increase resources.limits.memory

# 4. Enable garbage collection logging
# Add JVM option: -XX:+PrintGCDetails
```

### Prometheus Not Scraping Metrics

**Issue**: No metrics appearing in Prometheus dashboard

**Solutions**:
```bash
# 1. Verify Prometheus config
cat monitoring/prometheus.yml

# 2. Check target status in Prometheus UI
http://localhost:9090/targets

# 3. Verify services expose metrics endpoint
curl http://service:port/actuator/prometheus

# 4. Check Prometheus logs
docker logs prometheus
```

---

## Project Structure Details

### Common Library Structure
```
common-library/
├── src/main/java/com/ecommerce/common/
│   ├── config/
│   │   ├── PaginationConfig.java
│   │   └── PaginationProperties.java
│   ├── dto/               (Data Transfer Objects)
│   ├── entity/
│   │   └── BaseEntity.java
│   ├── enums/             (Enums: Role, PaymentMethod, etc.)
│   ├── events/            (Domain Events)
│   ├── exception/         (Custom Exceptions)
│   ├── kafka/             (Kafka Infrastructure)
│   └── security/          (Security Utilities & Filters)
└── pom.xml
```

### Service Structure (Example: Order Service)
```
order-service/
├── src/main/java/com/example/order_service/
│   ├── OrderServiceApplication.java
│   ├── client/            (Feign Clients to other services)
│   ├── config/
│   │   ├── RestClientConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/        (REST Endpoints)
│   ├── dto/               (DTOs for this service)
│   ├── entity/            (JPA Entities)
│   ├── event/             (Event Handlers)
│   ├── exception/         (Service-specific Exceptions)
│   ├── kafka/             (Kafka Producers/Consumers)
│   ├── repository/        (Spring Data Repositories)
│   ├── service/           (Business Logic)
│   └── util/              (Utility Classes)
├── src/main/resources/
│   └── application.properties
├── src/test/java/
├── pom.xml
└── Dockerfile (optional)
```

---

## Git Commits Reference

Recent commits (from git history):
- `db44357`: Support remote error messages in service unavailable exception
- `055cd63`: Throw ex with exceptions
- `a597096`: Propagate remote error details in Feign error decoder
- `ea933f7`: Persist shipping address snapshot when creating shipment
- `8053e41`: Return shipment response DTO from controller

---

## Contributing

1. Create feature branch from main
2. Make changes following coding standards
3. Add tests for new functionality
4. Ensure all tests pass
5. Create pull request with detailed description

---

## License

This project is proprietary and confidential.

---

## Support & Documentation

- **Eureka Dashboard**: http://localhost:8761
- **Zipkin Traces**: http://localhost:9411
- **Prometheus Metrics**: http://localhost:9090
- **Grafana Dashboards**: http://localhost:3000
- **AlertManager**: http://localhost:9093
- **API Gateway**: http://localhost:8087

For API documentation, services typically expose Swagger/OpenAPI:
- Auth Service: http://localhost:8080/swagger-ui.html
- Order Service: http://localhost:8088/swagger-ui.html
- (Configure based on springdoc-openapi integration)

---

**Last Updated**: 2026-09-23  
**Version**: 0.0.1-SNAPSHOT
