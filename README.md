# E-Commerce Microservices Project

A Spring Boot microservices e-commerce platform. Orders, payments, cart, and inventory are delivered as independently deployable services registered with Eureka and fronted by a reactive API gateway. Inter-service calls are synchronous over HTTP (OpenFeign) with Resilience4j circuit breakers; the **Transactional Outbox Pattern with Kafka** is implemented but currently **disabled** in code (see [Messaging & the Outbox](#messaging--the-outbox-pattern)).

## Architecture Overview

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Auth      │    │   Product   │    │    Order    │    │   Payment   │    │  Inventory  │
│  (Port 8090)│    │ (Port 8089) │    │ (Port 8088) │    │ (Port 8086) │    │  (Port 8083) │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │                  │                  │
       └──────────────────┴──────────┬───────┴──────────────────┴──────────────────┘
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

All external traffic enters through the API Gateway — `http://localhost:8087` locally, or `http://ecommerce.local` via the Kubernetes ingress. The gateway terminates JWT auth, rate-limits, and applies circuit breakers with fallbacks, then load-balances to downstream services via Eureka (`lb://SERVICE-NAME`). Auth endpoints go directly to the gateway too, so a single entry point serves the whole platform.

> **Cart service** is also part of the system (routes through the gateway at `/api/cart/**`); its port is set via `${SERVER_PORT}` and IntelliJ `run` config.

## Technology Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.3.12 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Gateway | 2023.0.3 (WebFlux-based) |
| Apache Kafka | 3.8.0 (client) |
| MySQL | 8.4 |
| Maven | 3.8+ |
| Hibernate (JPA) | 6.5.3 |
| Resilience4j | Latest (Feign `@CircuitBreaker`/`@Retry`) |
| OpenFeign | Latest |
| JJWT | 0.12.6 |

## Prerequisites

| Tool | Version | Check Command |
|------|---------|---------------|
| Java JDK | **21** | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| MySQL | 8.x | `mysql --version` |
| Docker | Latest | `docker --version` |

## Service Registry & Databases

### Eureka Server
Runs on `http://localhost:8761` by default (`application.yml` does not externalize this). The other services register with it and cache the registry (see per-service `application.yml`: Eureka config via `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}`).

### Databases
JPA creates all tables automatically via `ddl-auto`. Bogus placeholder databases are declared in `docker/mysql/init.sql` (`product_db`, `order_db`, `payment_db`, `auth_db`); the cart and inventory databases are **not** created by the init script — create them manually when running those services locally:

```bash
mysql -h 127.0.0.1 -u root -proot123 -e "
  CREATE DATABASE IF NOT EXISTS product_db;
  CREATE DATABASE IF NOT EXISTS order_db;
  CREATE DATABASE IF NOT EXISTS payment_db;
  CREATE DATABASE IF NOT EXISTS auth_db;
  CREATE DATABASE IF NOT EXISTS cart_db;
  CREATE DATABASE IF NOT EXISTS inventory_db;
"
```

## Startup Commands (Order Matters!)

All config is env-var driven; each service's `application.yml` references `${SERVER_PORT}`, `${SPRING_DATASOURCE_URL}`, `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}`, `${JWT_SECRET}`, `${MANAGEMENT_ZIPKIN_TRACING_ENDPOINT}`, etc. The values are supplied by `docker-compose.yml` (see below) or IntelliJ run configs. `docker compose up` starts MySQL, Zookeeper, Kafka, Eureka, and the five main services on one network.

### Step 1: Start MySQL + Kafka (Docker)

```bash
# Run the Docker stack (or just mysql + zookeeper + kafka)
docker compose up -d mysql zookeeper kafka

# Verify
docker ps --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}"
mysql -h 127.0.0.1 -u root -proot123 -e "SHOW DATABASES;"
```

### Step 2: Build the Project

```bash
cd "D:\MicroServices Project\ecommerce-microservices-project\ecommerce-microservices"

# Installs common-library first, then builds every module. Other modules won't compile without it.
mvn clean install -DskipTests
```

### Step 3: Start Services (each in its own terminal)

Eureka first, then the rest:

```bash
mvn spring-boot:run -pl eureka-server        # → http://localhost:8761
mvn spring-boot:run -pl auth-service         # → 8090
mvn spring-boot:run -pl product-service      # → 8089
mvn spring-boot:run -pl payment-service      # → 8086
mvn spring-boot:run -pl order-service        # → 8088
mvn spring-boot:run -pl inventory-service    # → inventory (needs inventory_db)
# mvn spring-boot:run -pl cart-service       # → cart (needs cart_db)
mvn spring-boot:run -pl api-gateway          # → 8087 (last — needs Eureka registry populated)
```

> When run directly with `mvn spring-boot:run`, Zookeeper and Kafka must already be running (from Docker). Without env vars set, the service will fail to start. Prefer the Docker/IntelliJ path.

### Verify All Services

```bash
jps -l                                   # Java processes
localhost:8761                            # Eureka dashboard (registered instances)
curl http://localhost:8087/actuator/health
curl http://localhost:8088/actuator/health
curl http://localhost:8086/actuator/health
curl http://localhost:8089/actuator/health
```

### Access via Ingress (`ecommerce.local`)

In Kubernetes, an nginx ingress routes the host **`http://ecommerce.local`** to the API Gateway (`:8087`). Point it at your cluster (`hosts`/`/etc/hosts` → the ingress controller's external IP) and hit the same paths above through it:

```bash
curl http://ecommerce.local/api/products
curl http://ecommerce.local/api/orders
```

The ingress definition lives in `helm/ecommerce-chart/templates/gateway-ingress.yaml` (Helm) and `kubernetes/ingress/gateway-ingress.yaml` (Kustomize).

## API Endpoints

All business endpoints are reached through the **API Gateway**. Locally use `http://localhost:8087`; through the ingress use `http://ecommerce.local` — both route to the same gateway routes below. Every request except `/api/auth/**`, `/actuator/**`, and `/eureka/**` requires a valid bearer JWT (see [Authentication](#authentication--authorization)).

### 1️⃣ Auth Service (`http://localhost:8090`, routed at `/api/auth/**`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/auth/register` | Register a user | Public |
| `POST` | `/api/auth/login` | Login — returns `accessToken`, `refreshToken`, and user info | Public |
| `POST` | `/api/auth/refresh` | Rotate refresh token | Public |
| `POST` | `/api/auth/logout` | Invalidate current session | Authenticated |
| `GET` | `/api/auth/sessions` | List my sessions | Authenticated |
| `DELETE` | `/api/auth/sessions/{id}` | Revoke one session | Authenticated |
| `DELETE` | `/api/auth/sessions` | Revoke all my sessions | Authenticated |
| `POST` | `/api/auth/forgot-password` | Request a password reset | Public |
| `POST` | `/api/auth/reset-password` | Perform password reset with token | Public |
| `GET` | `/api/roles` / `/api/roles/{roleId}` | List roles / one role | Authenticated |
| `GET` | `/api/roles/{roleId}/permissions` | Permissions of a role | Authenticated |
| `POST` | `/api/roles/{roleId}/permissions/{permissionId}` | Assign permission to role | Authenticated |
| `DELETE` | `/api/roles/{roleId}/permissions/{permissionId}` | Remove permission from role | Authenticated |
| `GET` | `/api/admin/audit/user/{userId}` | Security audit events by user | Admin |
| `GET` | `/api/admin/audit/event/{eventType}` | Security audit events by type | Admin |

> The marketing/onboarding sample controllers (`AdminTestController`, `CustomerTestController`, etc.) are development scratch pads, not the real surface.

### 2️⃣ Product Service (`:8089`, routed at `/api/products/**` and `/api/categories/**`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/products?search=&category=&page=&size=` | Paginated products, filterable by `search`/`category` | Admin, Customer |
| `GET` | `/api/products/{id}` | Get one product | Admin, Customer |
| `POST` | `/api/products` | Create product | Admin |
| `PUT` | `/api/products/{id}` | Update product | Admin |
| `DELETE` | `/api/products/{id}` | Deactivate product (soft delete) | Admin |
| `POST` | `/api/categories` | Create category | Admin |
| `GET` | `/api/categories` / `/api/categories/{id}` | List / get categories | Admin, Customer |
| `PUT` | `/api/categories/{id}` | Update category | Admin |
| `DELETE` | `/api/categories/{id}` | Deactivate category | Admin |

**CreateProductRequest:**
```json
{ "name": "iPhone 15", "description": "...", "price": 79999.00, "categoryId": 1, "sku": "APL15" }
```

**Product entity fields:** `id`, `name`, `description`, `price`, `category`, `sku`, `active`

### 3️⃣ Order Service (`:8088`, routed at `/api/orders/**`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/orders` | Place an order (body: `items[]`) | Admin, Customer |
| `GET` | `/api/orders` | All orders (paginated) | Admin |
| `GET` | `/api/orders/{id}` | Order by id — admin sees any, others only their own | Admin, Customer |
| `GET` | `/api/orders/my` | My orders (paginated) | Admin, Customer |
| `PUT` | `/api/orders/{id}` | Update order items (only while `PENDING_PAYMENT`) | Admin |
| `DELETE` | `/api/orders/{id}` | Cancel order → releases reserved inventory | Admin, Customer |
| `GET` | `/api/orders/status/{status}` | Orders by status | Admin, Customer |
| `POST` | `/api/orders/from-cart` | Create order from the caller's cart, then clear cart | Customer |

**CreateOrderRequest:**
```json
{
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

**OrderStatus enum:** `PENDING_PAYMENT`, `PAID`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`. Legal transitions are enforced by `OrderStatusLifecycle` (e.g. `PENDING_PAYMENT → PAID | CANCELLED`); invalid transitions throw.

**Important:** Order creation **reserves inventory** synchronously via the inventory service. If any reservation fails, previously reserved items are released and the order is rejected. Cancelling an order releases the reserved stock.

### 4️⃣ Payment Service (`:8086`, routed at `/api/payments/**`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/payments` | Create a payment for an order (`orderId`, `currency` ISO 3-letter, `paymentMethod`) | Admin, Customer |
| `GET` | `/api/payments` | All payments (paginated) | Admin |
| `GET` | `/api/payments/{id}` | Payment by id — admin sees any, others only their own | Admin, Customer |
| `GET` | `/api/payments/my` | My payments (paginated) | Admin, Customer |
| `GET` | `/api/payments/status/{status}` | Payments by status | Admin |
| `POST` | `/api/payments/webhook` | Provider webhook (idempotent, no auth annotation) | — |

**CreatePaymentRequest:**
```json
{
  "orderId": 1,
  "currency": "USD",
  "paymentMethod": "CARD"
}
```

**PaymentStatus enum:** `PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`, `CANCELLED`, `REFUNDED`.

Payment creation is transactional-safe: it writes a `PENDING` payment in one transaction, calls the configured provider (`MockPaymentProvider` by default) **outside** the transaction, then completes the payment in a second transaction. Webhooks update payment status via `PaymentStatusLifecycle` with optimistic-lock guards.

### 5️⃣ Inventory Service (`/api/inventory/**`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/inventory` | Create inventory record for a product | Admin |
| `GET` | `/api/inventory/{productId}` | Get stock | Admin |
| `POST` | `/api/inventory/{productId}/increase` | Increase stock | Admin |
| `POST` | `/api/inventory/{productId}/decrease` | Decrease stock | Admin |
| `POST` | `/api/inventory/{productId}/reserve` | Reserve stock for an order | Admin, Customer |
| `POST` | `/api/inventory/{productId}/release` | Release reserved stock | Admin, Customer |
| `POST` | `/api/inventory/{productId}/confirm` | Confirm a reservation (moves reserved → consumed) | Admin, Customer |

**InventoryResponse:**
```json
{ "id": 1, "productId": 1, "quantity": 100, "reservedQuantity": 2, "availableQuantity": 98 }
```

Stock rules: `reserve`/`decrease` fail if the requested amount exceeds available stock; `release` cannot exceed `reservedQuantity`; `confirm` moves stock out of reserved into consumed. The entity uses a `@Version` optimistic-lock column to detect concurrent modifications.

### 6️⃣ Cart Service (`/api/cart/**`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/cart` | Get my cart |
| `POST` | `/api/cart/items` | Add an item (`productId`, `quantity`) |
| `PUT` | `/api/cart/items/{productId}` | Update item quantity |
| `DELETE` | `/api/cart/items/{productId}` | Remove an item |
| `DELETE` | `/api/cart` | Clear cart |
| `GET` | `/api/cart/all` | All carts (paginated) — admin only |

## Authentication & Authorization

- **Issuer:** `auth-service` issues access + refresh JWTs (JJWT). Secrets and expiry are env-configured (`${JWT_SECRET}`, `${JWT_EXPIRATION}`). Session management, brute-force detection, and security audit events live here. Roles (`ROLE_ADMIN`, `ROLE_CUSTOMER`, `ROLE_SELLER`) are seeded on startup by `DataInitializer`.
- **Gateway enforcement:** `JwtAuthenticationFilter` (a Spring Cloud Gateway `GlobalFilter`) validates the bearer token on every request except public paths, then **strips any client-supplied identity headers** and **recreates** `X-Authenticated-User`, `X-Authenticated-User-Id`, and `X-User-Roles` from the validated JWT before forwarding. Never trust those headers if they arrive from outside the gateway.
- **Downstream trust:** each service imports `GatewaySecurityConfiguration` from `common-library` (`security.gateway.enabled=true`). Its `GatewayAuthenticationFilter` rebuilds a Spring `Authentication` from the gateway headers and populates the `SecurityContextHolder`. Callers read identity via `CurrentUser#getUserId` / `getUsername` (casts the principal to `GatewayUserPrincipal`).
- **Authorization:** `@PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")` on controller methods — the `roleSecurity` bean normalizes `ROLE_` prefixes and matches against the JWT-derived roles.
- **Feign calls between services** reuse the same headers via `FeignSecurityInterceptor` (used by `FeignSecurityConfiguration`). It forwards the identity headers plus `Authorization` and `X-Request-ID`. Downstream Feign errors are translated by `FeignErrorDecoder` into typed exceptions (`RemoteResourceNotFoundException` for 404, `RemoteServiceUnavailableException` for 5xx, etc.), which business services catch.

## Messaging & The Outbox Pattern

The **Transactional Outbox** with Kafka is implemented **but currently disabled**. The class skeletons exist in both order-service and payment-service (`OutboxPublisher`, `OrderKafkaProducer`, `OrderCreatedConsumer`, `PaymentCompletedProducer`, `PaymentOutboxScheduler`, `OutboxService`/`OutboxServiceImpl`, `PaymentCompletedConsumer`) and the `outbox_events` tables are in the schema, yet the Kafka producers/consumers/schedulers are commented out, and `spring-kafka` is the only live Kafka dependency. The intended flow (see the class comments and `KafkaTopics`):

```
POST /api/orders → Order Service saves Order + outbox row (ORDER_CREATED)
                  → OutboxPublisher (every 5s) → Kafka "order-created"
                  → OrderCreatedConsumer (payment) → creates Payment + outbox row (PAYMENT_COMPLETED)
                  → PaymentOutboxScheduler (every 5s) → Kafka "payment-completed"
                  → PaymentCompletedConsumer (order) → transitions order past PENDING_PAYMENT
```

Because the pipeline is off, **no code path currently moves an order out of `PENDING_PAYMENT`** — payment must be created via the synchronous API + webhook, and the order status must be advanced manually (or by re-enabling the outbox). Kafka topics (`KafkaTopics`): `order-created`, `payment-completed`, `inventory-updated`, `notification-sent`.

## Database Schema (JPA-managed)

Tables are created by Hibernate `ddl-auto`. Notable tables:

- **order_db:** `orders`, `order_items`, `outbox_events`
- **payment_db:** `payments`, `outbox_events`
- **product_db:** `products`, `categories`
- **auth_db:** `users`, `roles`, `permissions`, `user_sessions`, `password_reset_tokens`, `login_attempts`, `blocked_ips`, `security_audit_events`
- **inventory_db:** `inventories` (unique per `product_id`, `@Version` optimism lock)
- **cart_db:** `carts`, `cart_items`

## Resilience & Reliability

- **Gateway protections:** rate limiting (`RequestRateLimiter`, Redis-backed, keyed by IP) and `CircuitBreaker` with `fallbackUri` per route, plus a 2 MB request-size cap and a global `GlobalGatewayExceptionHandler`.
- **Feign resilience:** `@CircuitBreaker` on clients (e.g. cart's product lookup via `ProductServiceClient`). Order↔inventory has an explicit **compensation** design (`OrderInventoryHelperService`): reserve per item, and on failure release everything reserved so far; order *update* only reserves the delta and releases what's no longer needed; order *cancel* releases all. The Resilience4j instances configured in order-service's `application.yml` (`paymentService`, `paymentRetry`) are present but not yet attached to a live Feign client.
- **Transaction discipline:** external/provider calls are deliberately kept **outside** `@Transactional` methods (a named refactor: "separate provider call from transaction") — see `PaymentServiceImpl` (two transactions around the provider call) and the cart/inventory impls.
- **Concurrency:** optimistic locking (`@Version`) in inventory and payment; `ObjectOptimisticLockingFailureException` mapped to domain exceptions.

## Monitoring & Management

Actuator is enabled per service (health, metrics, prometheus). Distributed tracing via Micrometer + Zipkin (`management.tracing`, sampling probability from env). Each service logs with `traceId`/`spanId` in its log pattern and tags Prometheus metrics with `application=<service>`.

| Service | Health Endpoint |
|---------|-----------------|
| Eureka | `http://localhost:8761/` (dashboard) |
| API Gateway | `http://localhost:8087/actuator/health` |
| Order | `http://localhost:8088/actuator/health` |
| Product | `http://localhost:8089/actuator/health` |
| Payment | `http://localhost:8086/actuator/health` |

## Complete End-to-End Test

> Requires the full stack running (gateway + services + MySQL). Kafka is **not** required because the outbox pipeline is disabled.

```bash
# 0. Login to get a JWT (gateway public path)
curl -s -X POST http://localhost:8087/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"...","password":"..."}'          # → accessToken
TOKEN="<accessToken>"

# 1. Create a product (admin)
curl -s -X POST http://localhost:8087/api/products \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Test Product","price":100.00,"categoryId":1,"sku":"TEST100"}'

# 2. Create inventory for it (admin)
curl -s -X POST http://localhost:8087/api/inventory \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":100}'

# 3. Place an order → inventory is reserved (customer)
curl -s -X POST http://localhost:8087/api/orders \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'
# → Order with status PENDING_PAYMENT

# 4. Create a payment for it — provider is called, then status set
curl -s -X POST http://localhost:8087/api/payments \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"orderId":1,"currency":"USD","paymentMethod":"CARD"}'
# → Payment status SUCCESS (MockPaymentProvider)

# 5. Confirm inventory reservation (what 'order confirmed' should do)
curl -s -X POST http://localhost:8087/api/inventory/1/confirm \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"quantity":2}'
```

## Deployment / CI

- `docker-compose.yml` builds and runs the stack; the monitoring services (Zipkin, Prometheus, Grafana, Alertmanager) are present but commented out.
- `Jenkinsfile` (Windows `bat`): compile → test → SonarQube quality gate → package → `docker compose build` → tag/push `ankit9767/<service>:<BUILD_NUMBER>` → Helm deploy (`helm/ecommerce-chart`, namespace `ecommerce`) → kubectl verify.
- Raw Kubernetes manifests live under `kubernetes/` (services, HPA, monitoring); an nginx `Ingress` (same one Helms deploys) exposes the gateway at `http://ecommerce.local`.
- Build single images with per-service `Dockerfile`s (see `Docker Build All Services Command.txt`).
- Build single images with per-service `Dockerfile`s (see `Docker Build All Services Command.txt`).

## Troubleshooting

- **401 / "not authorized" when calling a business endpoint:** attach a valid bearer token from `/api/auth/login`, and hit the **gateway** (`:8087`), not the raw service port — the gateway injects the identity headers downstream services require. The raw service port also works for eureka/public paths only.
- **API returns 500 on order creation:** ensure the product exists and is `active`, inventory was created for it, and you didn't duplicate a productId within one order (throws `DuplicateOrderProductException`).
- **Gateway 404s right after startup:** the gateway needs time to discover routes via Eureka. Wait ~30s or restart the gateway.
- **`RemoteServiceUnavailableException` / circuit breaker trips on inter-service calls:** the downstream service is down (or slow). Check the Feign target service's health and Eureka registration.
- **`PaymentMethod` enum mismatch:** only `CARD`, `UPI`, `NET_BANKING`, `WALLET` are valid.