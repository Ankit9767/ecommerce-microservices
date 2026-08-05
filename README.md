# E-Commerce Microservices Project

A Spring Boot microservices e-commerce platform using the **Transactional Outbox Pattern** with Kafka for reliable async communication between services.

## Architecture Overview

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Product    │     │    Order     │     │   Payment    │
│   Service    │     │   Service    │     │   Service    │
│   (Port 8089)│     │   (Port 8088)│     │   (Port 8086)│
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       └────────────────────┼────────────────────┘
                            │
                     ┌──────▼───────┐
                     │  Eureka      │
                     │  Discovery   │
                     │  (Port 8761) │
                     └──────┬───────┘
                            │
                     ┌──────▼───────┐
                     │  API Gateway │
                     │  (Port 8087) │
                     └──────────────┘
```

### Event Flow (Transactional Outbox Pattern)

```
POST /api/orders → Order Service → Saves Order + Outbox Event (DB)
                                        │
                              OutboxPublisher (every 5s)
                                        │
                               Kafka: "order-created"
                                        │
                              OrderCreatedConsumer (Payment)
                                        │
                              → Creates Payment + Outbox Event (DB)
                                        │
                              PaymentOutboxScheduler (every 5s)
                                        │
                               Kafka: "payment-completed"
                                        │
                              PaymentCompletedConsumer (Order)
                                        │
                              → Updates Order Status → CONFIRMED
```

---

## Technology Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.3.12 |
| Spring Cloud | 2023.0.3 |
| Apache Kafka | 3.8.0 |
| MySQL | 8.x |
| Maven | 3.8+ |
| Hibernate (JPA) | 6.5.3 |
| Resilience4j | Latest |
| OpenFeign | Latest |

---

## Prerequisites

| Tool | Version | Check Command |
|------|---------|---------------|
| Java JDK | **21** | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| MySQL | 8.x | `mysql --version` |
| Docker | Latest | `docker --version` |

---

## Startup Commands (Order Matters!)

### Step 1: Start Kafka (Docker Container)

```bash
# Navigate to Docker compose directory
cd "D:\MicroServices Project\Docker\Kafka"

# Start Kafka in detached mode
docker compose up -d

# Verify Kafka is running
docker ps --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}"
# Expected: kafka, port 9092, Up/Healthy
```

### Step 2: Verify MySQL is Running

```bash
# Check MySQL connection
mysql -h 127.0.0.1 -u root -proot123 -e "SHOW DATABASES;"

# Expected databases should include (created automatically by JPA):
# - order_db
# - payment_db
# - product_db

# If databases don't exist yet, create them manually:
mysql -h 127.0.0.1 -u root -proot123 -e "
  CREATE DATABASE IF NOT EXISTS order_db;
  CREATE DATABASE IF NOT EXISTS payment_db;
  CREATE DATABASE IF NOT EXISTS product_db;
"
```

### Step 3: Build the Project

```bash
cd "D:\MicroServices Project\ecommerce-microservices-project\ecommerce-microservices"

# Clean install all modules (this also compiles common-library required by other services)
mvn clean install -DskipTests
```

### Step 4: Start Eureka Server (Service Registry)

```bash
# Open Terminal 1
cd "D:\MicroServices Project\ecommerce-microservices-project\ecommerce-microservices"

# Using Maven (with logging to file):
mvn spring-boot:run -pl eureka-server > eureka-server.log 2>&1

# OR using the packaged JAR:
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar

# Verify: http://localhost:8761/ (Eureka Dashboard)
```

### Step 5: Start Product Service

```bash
# Open Terminal 2
cd "D:\MicroServices Project\ecommerce-microservices-project\ecommerce-microservices"

mvn spring-boot:run -pl product-service > product-service.log 2>&1

# OR using JAR:
java -jar product-service/target/product-service-0.0.1-SNAPSHOT.jar
```

### Step 6: Start Payment Service

```bash
# Open Terminal 3
cd "D:\MicroServices Project\ecommerce-microservices-project\ecommerce-microservices"

mvn spring-boot:run -pl payment-service > payment-service.log 2>&1

# OR using JAR:
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
```

### Step 7: Start Order Service

```bash
# Open Terminal 4
cd "D:\MicroServices Project\ecommerce-microservices-project\ecommerce-microservices"

mvn spring-boot:run -pl order-service > order-service.log 2>&1

# OR using JAR:
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
```

### Step 8: Start API Gateway

```bash
# Open Terminal 5
cd "D:\MicroServices Project\ecommerce-microservices-project\ecommerce-microservices"

mvn spring-boot:run -pl api-gateway > api-gateway.log 2>&1

# OR using JAR:
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

### Verify All Services Are Running

```bash
# Check Java processes
jps -l
# Expected:
# - com.example.eureka_server.EurekaServerApplication
# - com.example.product_service.ProductServiceApplication
# - com.example.order_service.OrderServiceApplication
# - com.example.payment_service.PaymentServiceApplication
# - com.example.api_gateway.ApiGatewayApplication

# Check ports
netstat -ano | findstr "8761 8089 8088 8086 8087"
# 8761 - Eureka
# 8089 - Product Service
# 8088 - Order Service
# 8086 - Payment Service
# 8087 - API Gateway

# Health check endpoints:
curl http://localhost:8761/       # Eureka Dashboard (browser)
curl http://localhost:8088/actuator/health
curl http://localhost:8086/actuator/health
curl http://localhost:8089/actuator/health
curl http://localhost:8087/actuator/health
```

---

## API Endpoints

### 1️⃣ Product Service (`http://localhost:8089`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `GET` | `/api/products` | Get all products | — | `List<ProductResponse>` |
| `GET` | `/api/products/{id}` | Get product by ID | — | `ProductResponse` |
| `POST` | `/api/products` | Create a new product | `CreateProductRequest` | `ProductResponse` (201) |
| `PUT` | `/api/products/{id}` | Update a product | `Product` | `Product` |
| `DELETE` | `/api/products/{id}` | Delete a product | — | 204 No Content |

**CreateProductRequest:**
```json
{
  "name": "iPhone 15",
  "price": 79999.00,
  "category": "Electronics"
}
```

**Product Entity fields:** `id`, `name`, `description`, `price`, `category`, `sku`, `active`

**ProductResponse (from product-service DTO):**
```json
{
  "id": 1,
  "name": "iPhone 15",
  "price": 79999.00,
  "category": "Electronics"
}
```

**Example:**
```bash
# Create a product
curl -X POST http://localhost:8089/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Product", "price": 100.00, "category": "Electronics"}'

# Get all products
curl http://localhost:8089/api/products
```

---

### 2️⃣ Order Service (`http://localhost:8088`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `GET` | `/api/orders` | Get all orders | — | `List<OrderResponse>` |
| `GET` | `/api/orders/{id}` | Get order by ID | — | `OrderResponse` |
| `POST` | `/api/orders` | Place a new order | `CreateOrderRequest` | `OrderResponse` (201) |
| `PUT` | `/api/orders/{id}` | Update an order | `Order` | `Order` |
| `DELETE` | `/api/orders/{id}` | Delete an order | — | 204 No Content |

**CreateOrderRequest:**
```json
{
  "productId": 1,
  "customerId": 100,
  "quantity": 2,
  "paymentMethod": "CARD"
}
```

**PaymentMethod enum values:** `CARD`, `UPI`, `NET_BANKING`, `WALLET`

**OrderResponse:**
```json
{
  "id": 1,
  "productId": 1,
  "customerId": 100,
  "quantity": 2,
  "totalAmount": 200.00,
  "status": "PENDING_PAYMENT",
  "createdAt": "2026-07-18T10:24:14.886823Z"
}
```

**OrderStatus enum values:** `PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`, `PAYMENT_FAILED`

**Example:**
```bash
# Place an order (triggers outbox → Kafka → Payment → outbox → Kafka → CONFIRMED)
curl -X POST http://localhost:8088/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "customerId": 100, "quantity": 2, "paymentMethod": "CARD"}'

# Get all orders
curl http://localhost:8088/api/orders

# Get order by ID
curl http://localhost:8088/api/orders/1
```

---

### 3️⃣ Payment Service (`http://localhost:8086`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `POST` | `/payments` | Process a payment | `PaymentRequest` | `PaymentResponse` (201) |
| `GET` | `/payments/{id}` | Get payment by ID | — | `PaymentResponse` |
| `GET` | `/payments` | Get all payments | — | `List<PaymentResponse>` |

**PaymentRequest:**
```json
{
  "orderId": 1,
  "amount": 200.00,
  "paymentMethod": "CARD"
}
```

**PaymentResponse:**
```json
{
  "paymentId": 1,
  "orderId": 1,
  "amount": 200.00,
  "paymentMethod": "CARD",
  "status": "SUCCESS",
  "transactionId": "TXN-865784CA"
}
```

**PaymentStatus enum values:** `PENDING`, `SUCCESS`, `FAILED`

**Example:**
```bash
# Process a payment directly
curl -X POST http://localhost:8086/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId": 1, "amount": 200.00, "paymentMethod": "CARD"}'

# Get all payments
curl http://localhost:8086/payments
```

---

### 4️⃣ API Gateway (`http://localhost:8087`)

The gateway routes requests to services via Eureka load balancing:

| Gateway Path | Target Service |
|--------------|---------------|
| `/orders/**` | `lb://ORDER-SERVICE` (→ port 8088) |
| `/products/**` | `lb://PRODUCT-SERVICE` (→ port 8089) |

> **Note:** There's no `/payments/**` route in the gateway. Access payment endpoints directly at `http://localhost:8086/payments`.

**Example via Gateway:**
```bash
# Create product via gateway
curl -X POST http://localhost:8087/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Product", "price": 100.00, "category": "Electronics"}'

# Create order via gateway
curl -X POST http://localhost:8087/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "customerId": 100, "quantity": 2, "paymentMethod": "CARD"}'
```

---

## Kafka Topics

| Topic Name | Producer | Consumer | Event Class |
|------------|----------|----------|-------------|
| `order-created` | Order Service (`OutboxPublisher`) | Payment Service (`OrderCreatedConsumer`) | `OrderCreatedEvent` |
| `payment-completed` | Payment Service (`PaymentOutboxScheduler`) | Order Service (`PaymentCompletedConsumer`) | `PaymentCompletedEvent` |

### Event Payloads

**OrderCreatedEvent:**
```
{
  "orderId": 1,
  "customerId": 100,
  "productId": 1,
  "quantity": 2,
  "amount": 200.00,
  "paymentMethod": "CARD"
}
```

**PaymentCompletedEvent:**
```
{
  "paymentId": 1,
  "orderId": 1,
  "amount": 200.00,
  "paymentMethod": "CARD",
  "paymentStatus": "SUCCESS",
  "transactionId": "TXN-865784CA"
}
```

---

## Database Schema

### `order_db` — Order Service

**Table: `orders`**
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT (PK) | Auto-increment |
| `product_id` | BIGINT | |
| `customer_id` | BIGINT | |
| `quantity` | INT | |
| `total_amount` | DECIMAL(10,2) | |
| `status` | VARCHAR(255) | Enum: PENDING_PAYMENT, CONFIRMED, CANCELLED, PAYMENT_FAILED |
| `created_at` | DATETIME | Auto-set |
| `updated_at` | DATETIME | Auto-updated |

**Table: `outbox_events`**
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT (PK) | Auto-increment |
| `event_type` | VARCHAR(255) | `"ORDER_CREATED"` |
| `aggregate_id` | BIGINT | Order ID |
| `payload` | TEXT | JSON of `OrderCreatedEvent` |
| `published` | BOOLEAN | `false` = pending publish |
| `created_at` | DATETIME | |

### `payment_db` — Payment Service

**Table: `payments`**
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT (PK) | Auto-increment |
| `order_id` | BIGINT | Unique |
| `amount` | DECIMAL(10,2) | |
| `payment_method` | VARCHAR(255) | Enum: CARD, UPI, NET_BANKING, WALLET |
| `status` | VARCHAR(255) | Enum: PENDING, SUCCESS, FAILED |
| `transaction_id` | VARCHAR(255) | e.g., `TXN-865784CA` |
| `created_at` | DATETIME | |

**Table: `outbox_events`**
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT (PK) | Auto-increment |
| `event_type` | VARCHAR(255) | `"PAYMENT_COMPLETED"` |
| `aggregate_id` | BIGINT | Order ID |
| `payload` | TEXT | JSON of `PaymentCompletedEvent` |
| `published` | BOOLEAN | `false` = pending publish |
| `created_at` | DATETIME | |

### `product_db` — Product Service

**Table: `products`**
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT (PK) | Auto-increment |
| `name` | VARCHAR(255) | |
| `description` | VARCHAR(255) | |
| `price` | DECIMAL(10,2) | |
| `category` | VARCHAR(255) | |
| `sku` | VARCHAR(255) | |
| `active` | BOOLEAN | |

---

## Monitoring & Management

### Actuator Endpoints (available on each service)

| Service | Health Endpoint | Port |
|---------|----------------|------|
| Order Service | `http://localhost:8088/actuator/health` | 8088 |
| Payment Service | `http://localhost:8086/actuator/health` | 8086 |
| Product Service | `http://localhost:8089/actuator/health` | 8089 |
| API Gateway | `http://localhost:8087/actuator/health` | 8087 |
| Eureka Server (Dashboard) | `http://localhost:8761/` | 8761 |

### Eureka Dashboard
Open `http://localhost:8761/` in a browser to see all registered services.

---

## Transactional Outbox Pattern — How It Works

### Order Creation (Async Path)

1. **`POST /api/orders`** → `OrderServiceImpl.createOrder()`
   - Fetches product details via Feign `ProductClient`
   - Calculates total amount
   - Saves `Order` with status `PENDING_PAYMENT`
   - Writes `ORDER_CREATED` event to `order_db.outbox_events` (published = false)
   - Returns the order response

2. **`OutboxPublisher`** (Runs every 5 seconds)
   - Queries `outbox_events WHERE published = false`
   - Deserializes payload → `OrderCreatedEvent`
   - Publishes to Kafka topic `order-created`
   - Marks outbox row as `published = true`

3. **`OrderCreatedConsumer`** (Payment Service, group: `payment-group`)
   - Receives `OrderCreatedEvent` from Kafka
   - Checks if payment already exists for this order (idempotency guard)
   - Creates `Payment` record with `status = SUCCESS`
   - Writes `PAYMENT_COMPLETED` event to `payment_db.outbox_events` (published = false)

4. **`PaymentOutboxScheduler`** (Runs every 5 seconds)
   - Queries unpaid outbox events
   - Publishes to Kafka topic `payment-completed`
   - Marks outbox row as `published = true`

5. **`PaymentCompletedConsumer`** (Order Service, group: `order-group`)
   - Receives `PaymentCompletedEvent`
   - Updates order status to `CONFIRMED` (or `PAYMENT_FAILED`)

### Resilience Features

- **Circuit Breaker (`paymentService`)**: 10-window sliding, 50% failure threshold, 20s open-state
- **Retry (`paymentRetry`)**: 3 attempts, 2s initial wait, exponential backoff
- **Retryable Kafka Consumer**: 4 attempts, 3s backoff, dead-letter topic (`-dlt`)
- **Fallback methods**: Graceful degradation if payment service is unavailable

---

## Complete End-to-End Test

```bash
# 1. Create a product (if none exists)
curl -X POST http://localhost:8089/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Product", "price": 100.00, "category": "Electronics"}'

# 2. Place an order
curl -s -X POST http://localhost:8088/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "customerId": 100, "quantity": 2, "paymentMethod": "CARD"}'
# Returns: Order with status PENDING_PAYMENT

# 3. Wait ~10 seconds for outbox schedulers to process

# 4. Verify order is now CONFIRMED
curl http://localhost:8088/api/orders/1
# Check: "status": "CONFIRMED"

# 5. Verify payment was created
curl http://localhost:8086/payments
# Check: payment with transactionId exists

# 6. Check outbox events (all should be published)
mysql -h 127.0.0.1 -u root -proot123 order_db \
  -e "SELECT id, event_type, published, created_at FROM outbox_events;"
mysql -h 127.0.0.1 -u root -proot123 payment_db \
  -e "SELECT id, event_type, published, created_at FROM outbox_events;"
# All rows should have published = true
```

---

## Troubleshooting

### Issue: API returns 500 on order creation
**Cause:** `PaymentMethod` enum mismatch — only `CARD`, `UPI`, `NET_BANKING`, `WALLET` are valid.
**Fix:** Use `"CARD"` instead of `"CREDIT_CARD"`.

### Issue: Product Feign call fails with deserialization error
**Cause:** The `common-library` `ProductResponse` record expects a `stock` field but the product service returns `category`.
**Fix:** The `@JsonIgnoreProperties(ignoreUnknown = true)` annotation has been added.

### Issue: API Gateway returns 404
**Cause:** Gateway needs time to register with Eureka and discover services.
**Fix:** Wait ~30 seconds after starting services, or restart the gateway.

### Issue: Kafka messages not being consumed
**Cause:** Consumer might be in a different group or topic doesn't exist yet.
**Fix:** Kafka auto-creates topics on first produce. Check consumer group IDs:
- order-service: `order-group`
- payment-service: `payment-group`

### Issue: Services not registering with Eureka
**Fix:** Ensure Eureka Server is running first before starting other services.
