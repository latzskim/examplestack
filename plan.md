# Simple Shop - Project Plan

## Overview

A production-ready e-commerce application built with Java 25, following DDD, Hexagonal Architecture (Ports & Adapters), and Clean Code principles. Uses Spring Modulith for modular monolith architecture.

---

## Technology Stack

| Category | Technology                                    |
|----------|-----------------------------------------------|
| Language | Java 25                                       |
| Framework | Spring Boot 3.4+, Spring Modulith             |
| Web | Spring MVC, Thymeleaf                         |
| Security | Spring Security 6                             |
| Database | PostgreSQL 18                                 |
| Cache | Redis                                         |
| ORM | Spring Data JPA / Hibernate 6                 |
| Testing | TestNG (unit), JUnit 5 + Testcontainers (e2e) |
| Tracing | Jaeger + Micrometer Tracing                   |
| Email | Mailhog (local catch)                         |
| Containers | Docker, Docker Compose, Testcontainers        |

---

## Architecture

### Hexagonal Architecture (per module)

```
┌─────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  Controllers │  │  JPA Repos   │  │  External Services   │   │
│  │  (Web/API)   │  │  (Adapters)  │  │  (Email, Cache)      │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘   │
│         │                 │                      │               │
│  ┌──────▼─────────────────▼──────────────────────▼───────────┐  │
│  │                      PORTS                                 │  │
│  │   ┌─────────────────┐          ┌─────────────────────┐    │  │
│  │   │   Input Ports   │          │   Output Ports      │    │  │
│  │   │   (Use Cases)   │          │   (Repositories)    │    │  │
│  │   └────────┬────────┘          └──────────┬──────────┘    │  │
│  └────────────┼──────────────────────────────┼───────────────┘  │
│               │                              │                   │
│  ┌────────────▼──────────────────────────────▼───────────────┐  │
│  │                        DOMAIN                              │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────────────┐   │  │
│  │  │ Aggregates │  │  Entities  │  │  Value Objects     │   │  │
│  │  │            │  │            │  │  Domain Events     │   │  │
│  │  │            │  │            │  │  Domain Services   │   │  │
│  │  └────────────┘  └────────────┘  └────────────────────┘   │  │
│  └────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Module Structure (Spring Modulith)

```
com.simpleshop
├── catalog/        # Product definitions, categories
├── inventory/      # Warehouse, stock management
├── cart/           # Shopping cart (anonymous + authenticated)
├── order/          # Purchase transactions
├── identity/       # Users, authentication, roles
├── shipping/       # Shipment tracking
├── notification/   # Email notifications
└── shared/         # Shared kernel (common types, events)
```

---

## Bounded Contexts & Domains

### 1. Catalog Module (`catalog`)

**Responsibility**: Product definitions and categories management.

**Aggregate Roots**:
- `ProductDefinition` - template for sellable products
- `Category` - product categorization

**Entities**:
- `ProductDefinition`
  - `id: ProductDefinitionId` (UUID)
  - `name: ProductName`
  - `description: ProductDescription`
  - `price: Money`
  - `categoryId: CategoryId`
  - `imageUrl: ImageUrl`
  - `status: ProductStatus` (ACTIVE, INACTIVE)
  - `createdAt: Instant`
  - `updatedAt: Instant`

- `Category`
  - `id: CategoryId` (UUID)
  - `name: CategoryName`
  - `parentCategoryId: CategoryId` (nullable, for hierarchy)

**Value Objects**:
- `ProductDefinitionId`
- `ProductName`
- `ProductDescription`
- `Money` (amount + currency)
- `CategoryId`
- `CategoryName`
- `ImageUrl`
- `ProductStatus`

**Domain Events**:
- `ProductDefinitionCreated`
- `ProductDefinitionUpdated`
- `ProductDefinitionDeactivated`

**Ports**:
```java
// Input Ports (Use Cases)
interface CreateProductDefinition
interface UpdateProductDefinition
interface DeactivateProductDefinition
interface GetProductDefinition
interface ListProductDefinitions
interface CreateCategory
interface ListCategories

// Output Ports
interface ProductDefinitionRepository
interface CategoryRepository
interface ProductCachePort  // Redis cache
```

**CQRS**:
- Commands: `CreateProductDefinitionCommand`, `UpdateProductDefinitionCommand`, `DeactivateProductDefinitionCommand`
- Queries: `GetProductDefinitionQuery`, `ListProductDefinitionsQuery`, `SearchProductsQuery`
- Read Models: `ProductDefinitionView`, `ProductListItemView`, `CategoryView`

---

### 2. Inventory Module (`inventory`)

**Responsibility**: Stock management, warehouse locations, product instances.

**Aggregate Roots**:
- `Warehouse`
- `Stock` (per product definition per warehouse)

**Entities**:
- `Warehouse`
  - `id: WarehouseId` (UUID)
  - `name: WarehouseName`
  - `location: Address`

- `Stock`
  - `id: StockId` (UUID)
  - `productDefinitionId: ProductDefinitionId`
  - `warehouseId: WarehouseId`
  - `quantity: Quantity`
  - `reservedQuantity: Quantity`

**Value Objects**:
- `WarehouseId`
- `WarehouseName`
- `Address` (street, city, postalCode, country)
- `StockId`
- `Quantity`

**Domain Events**:
- `StockReplenished`
- `StockReserved`
- `StockReleased`
- `StockDepleted`

**Ports**:
```java
// Input Ports
interface ReserveStock
interface ReleaseStock
interface ReplenishStock
interface CheckStockAvailability

// Output Ports
interface StockRepository
interface WarehouseRepository
```

**CQRS**:
- Commands: `ReserveStockCommand`, `ReleaseStockCommand`, `ReplenishStockCommand`
- Queries: `CheckStockAvailabilityQuery`, `GetWarehouseStockQuery`
- Read Models: `StockView`, `ProductAvailabilityView`

---

### 3. Cart Module (`cart`)

**Responsibility**: Shopping cart management for anonymous and authenticated users.

**Aggregate Roots**:
- `Cart`

**Entities**:
- `Cart`
  - `id: CartId` (UUID)
  - `sessionId: SessionId` (for anonymous users)
  - `userId: UserId` (nullable, for authenticated users)
  - `items: List<CartItem>`
  - `createdAt: Instant`
  - `updatedAt: Instant`

- `CartItem`
  - `productDefinitionId: ProductDefinitionId`
  - `quantity: Quantity`
  - `priceAtAddition: Money`

**Value Objects**:
- `CartId`
- `SessionId`
- `CartItemId`

**Domain Events**:
- `ItemAddedToCart`
- `ItemRemovedFromCart`
- `ItemQuantityUpdated`
- `CartCleared`
- `CartMerged` (when anonymous user logs in)

**Ports**:
```java
// Input Ports
interface AddItemToCart
interface RemoveItemFromCart
interface UpdateItemQuantity
interface GetCart
interface ClearCart
interface MergeAnonymousCart

// Output Ports
interface CartRepository
```

**CQRS**:
- Commands: `AddItemToCartCommand`, `RemoveItemFromCartCommand`, `UpdateItemQuantityCommand`, `ClearCartCommand`
- Queries: `GetCartQuery`, `GetCartTotalQuery`
- Read Models: `CartView`, `CartItemView`, `CartSummaryView`

---

### 4. Order Module (`order`)

**Responsibility**: Purchase transactions, order lifecycle.

**Aggregate Roots**:
- `Order`

**Entities**:
- `Order`
  - `id: OrderId` (UUID)
  - `orderNumber: OrderNumber`
  - `userId: UserId`
  - `items: List<OrderItem>`
  - `shippingAddress: Address`
  - `status: OrderStatus`
  - `totalAmount: Money`
  - `createdAt: Instant`
  - `paidAt: Instant`

- `OrderItem`
  - `productDefinitionId: ProductDefinitionId`
  - `productName: ProductName`
  - `quantity: Quantity`
  - `unitPrice: Money`
  - `warehouseId: WarehouseId`

**Value Objects**:
- `OrderId`
- `OrderNumber` (human-readable, e.g., ORD-2024-00001)
- `OrderStatus` (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)

**Domain Events**:
- `OrderPlaced`
- `OrderConfirmed`
- `OrderCancelled`
- `OrderShipped`
- `OrderDelivered`

**Ports**:
```java
// Input Ports
interface PlaceOrder
interface ConfirmOrder
interface CancelOrder
interface GetOrder
interface ListUserOrders

// Output Ports
interface OrderRepository
```

**CQRS**:
- Commands: `PlaceOrderCommand`, `ConfirmOrderCommand`, `CancelOrderCommand`
- Queries: `GetOrderQuery`, `ListUserOrdersQuery`, `GetOrderByNumberQuery`
- Read Models: `OrderView`, `OrderSummaryView`, `OrderItemView`

**Saga/Process**:
- `PurchaseSaga`: Coordinates order placement → stock reservation → order confirmation → notification

---

### 5. Identity Module (`identity`)

**Responsibility**: User management, authentication, authorization.

**Aggregate Roots**:
- `User`

**Entities**:
- `User`
  - `id: UserId` (UUID)
  - `email: Email`
  - `passwordHash: PasswordHash`
  - `firstName: FirstName`
  - `lastName: LastName`
  - `role: Role`
  - `status: UserStatus`
  - `createdAt: Instant`
  - `lastLoginAt: Instant`

**Value Objects**:
- `UserId`
- `Email`
- `PasswordHash`
- `FirstName`
- `LastName`
- `Role` (USER, ADMIN)
- `UserStatus` (ACTIVE, INACTIVE, LOCKED)

**Domain Events**:
- `UserRegistered`
- `UserLoggedIn`
- `UserLoggedOut`
- `PasswordChanged`
- `UserDeactivated`

**Ports**:
```java
// Input Ports
interface RegisterUser
interface AuthenticateUser
interface GetUserProfile
interface ChangePassword
interface DeactivateUser

// Output Ports
interface UserRepository
interface PasswordEncoder
```

**Spring Security Integration**:
- Custom `UserDetailsService` implementation
- Role-based access control (RBAC)
- Session management
- CSRF protection

---

### 6. Shipping Module (`shipping`)

**Responsibility**: Shipment creation, tracking, status updates.

**Aggregate Roots**:
- `Shipment`

**Entities**:
- `Shipment`
  - `id: ShipmentId` (UUID)
  - `trackingNumber: TrackingNumber`
  - `orderId: OrderId`
  - `warehouseId: WarehouseId`
  - `destinationAddress: Address`
  - `status: ShipmentStatus`
  - `statusHistory: List<ShipmentStatusChange>`
  - `estimatedDelivery: LocalDate`
  - `createdAt: Instant`

- `ShipmentStatusChange`
  - `status: ShipmentStatus`
  - `changedAt: Instant`
  - `location: String`
  - `notes: String`

**Value Objects**:
- `ShipmentId`
- `TrackingNumber` (e.g., SHIP-2024-XXXXX)
- `ShipmentStatus` (CREATED, PICKED, PACKED, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED)

**Domain Events**:
- `ShipmentCreated`
- `ShipmentStatusUpdated`
- `ShipmentDelivered`
- `ShipmentFailed`

**Ports**:
```java
// Input Ports
interface CreateShipment
interface UpdateShipmentStatus
interface GetShipment
interface TrackShipment

// Output Ports
interface ShipmentRepository
```

**CQRS**:
- Commands: `CreateShipmentCommand`, `UpdateShipmentStatusCommand`
- Queries: `GetShipmentQuery`, `TrackShipmentQuery`, `GetShipmentsByOrderQuery`
- Read Models: `ShipmentView`, `ShipmentTrackingView`, `ShipmentStatusHistoryView`

---

### 7. Notification Module (`notification`)

**Responsibility**: Email notifications, templates.

**Aggregate Roots**:
- `NotificationTemplate`
- `NotificationLog`

**Entities**:
- `NotificationLog`
  - `id: NotificationId`
  - `type: NotificationType`
  - `recipientEmail: Email`
  - `subject: String`
  - `status: NotificationStatus`
  - `sentAt: Instant`
  - `errorMessage: String`

**Value Objects**:
- `NotificationId`
- `NotificationType` (ORDER_CONFIRMATION, SHIPMENT_UPDATE, INVOICE)
- `NotificationStatus` (PENDING, SENT, FAILED)

**Domain Events**:
- `NotificationSent`
- `NotificationFailed`

**Ports**:
```java
// Input Ports
interface SendOrderConfirmation
interface SendShipmentNotification
interface SendInvoice

// Output Ports
interface EmailSender
interface NotificationLogRepository
interface InvoiceGenerator
```

**Email Templates** (Thymeleaf):
- `order-confirmation.html`
- `shipment-update.html`
- `invoice.html`

---

### 8. Shared Kernel (`shared`)

**Common Types**:
```java
// Base classes
abstract class AggregateRoot<ID>
abstract class Entity<ID>
abstract class ValueObject
interface DomainEvent

// Common value objects
record Money(BigDecimal amount, Currency currency)
record Address(String street, String city, String postalCode, String country)
record Email(String value)

// Common interfaces
interface UseCase<I, O>
interface Command
interface Query<R>
interface CommandHandler<C extends Command>
interface QueryHandler<Q extends Query<R>, R>

// Pagination
record Page<T>(List<T> content, int page, int size, long totalElements)
record PageRequest(int page, int size, String sortBy, SortDirection direction)
```

---

## Module Dependencies (Spring Modulith)

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│                    (Controllers, Views)                      │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                      Application                             │
├─────────┬─────────┬─────────┬─────────┬─────────┬───────────┤
│ catalog │inventory│  cart   │  order  │shipping │notification│
│         │         │         │         │         │           │
│    ◄────┼────►    │    ◄────┼────►    │    ◄────┼────►      │
│         │         │         │         │         │           │
└────┬────┴────┬────┴────┬────┴────┬────┴────┬────┴─────┬─────┘
     │         │         │         │         │          │
     └─────────┴─────────┴────┬────┴─────────┴──────────┘
                              │
                    ┌─────────▼─────────┐
                    │      shared       │
                    │  (Shared Kernel)  │
                    └───────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │     identity      │
                    │ (Cross-cutting)   │
                    └───────────────────┘
```

**Event-based Communication**:
```
catalog ──ProductDefinitionCreated──► inventory (create initial stock)
order ────OrderPlaced─────────────────► inventory (reserve stock)
order ────OrderConfirmed──────────────► shipping (create shipment)
order ────OrderConfirmed──────────────► notification (send confirmation)
shipping ─ShipmentStatusUpdated───────► notification (send update)
shipping ─ShipmentCreated─────────────► notification (send tracking link)
identity ─UserRegistered──────────────► notification (send welcome email)
```

---

## Package Structure (per module)

```
com.simpleshop.{module}/
├── domain/
│   ├── model/
│   │   ├── {AggregateName}.java
│   │   ├── {EntityName}.java
│   │   └── vo/
│   │       └── {ValueObject}.java
│   ├── event/
│   │   └── {DomainEvent}.java
│   ├── service/
│   │   └── {DomainService}.java
│   └── exception/
│       └── {DomainException}.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── {UseCase}.java
│   │   └── out/
│   │       └── {Port}.java
│   ├── service/
│   │   └── {ApplicationService}.java
│   ├── command/
│   │   └── {Command}.java
│   └── query/
│       ├── {Query}.java
│       └── {ReadModel}.java
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       └── {Controller}.java
    │   └── out/
    │       ├── persistence/
    │       │   ├── {JpaRepository}.java
    │       │   ├── {JpaEntity}.java
    │       │   └── {RepositoryAdapter}.java
    │       └── cache/
    │           └── {CacheAdapter}.java
    └── config/
        └── {ModuleConfig}.java
```

---

## Database Schema

### Tables

```sql
-- Catalog
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_category_id UUID REFERENCES categories(id),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE product_definitions (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price_amount DECIMAL(19,4) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    category_id UUID REFERENCES categories(id),
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Inventory
CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    street VARCHAR(200),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE stocks (
    id UUID PRIMARY KEY,
    product_definition_id UUID NOT NULL REFERENCES product_definitions(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(product_definition_id, warehouse_id)
);

-- Identity
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP
);

-- Cart
CREATE TABLE carts (
    id UUID PRIMARY KEY,
    session_id VARCHAR(100),
    user_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_definition_id UUID NOT NULL REFERENCES product_definitions(id),
    quantity INT NOT NULL,
    price_at_addition_amount DECIMAL(19,4) NOT NULL,
    price_at_addition_currency VARCHAR(3) NOT NULL,
    added_at TIMESTAMP NOT NULL
);

-- Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    shipping_street VARCHAR(200),
    shipping_city VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    total_amount DECIMAL(19,4) NOT NULL,
    total_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_definition_id UUID NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price_amount DECIMAL(19,4) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,
    warehouse_id UUID NOT NULL
);

-- Shipping
CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    tracking_number VARCHAR(50) NOT NULL UNIQUE,
    order_id UUID NOT NULL REFERENCES orders(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    destination_street VARCHAR(200),
    destination_city VARCHAR(100),
    destination_postal_code VARCHAR(20),
    destination_country VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    estimated_delivery DATE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE shipment_status_history (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    location VARCHAR(200),
    notes TEXT
);

-- Notification
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_product_definitions_category ON product_definitions(category_id);
CREATE INDEX idx_product_definitions_status ON product_definitions(status);
CREATE INDEX idx_stocks_product ON stocks(product_definition_id);
CREATE INDEX idx_carts_session ON carts(session_id);
CREATE INDEX idx_carts_user ON carts(user_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_number);
```

---

## Security Configuration

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| ANONYMOUS | Browse products, Add to cart, View cart, Register, Login |
| USER | All anonymous + Checkout, View orders, Track shipments, Logout |
| ADMIN | All user + CRUD product definitions, View admin panel |

### URL Security Mapping

```java
// Public (anonymous)
permitAll: GET /products/**, GET /categories/**, /cart/**, /login, /register, /

// Authenticated (USER + ADMIN)
authenticated: POST /checkout, GET /orders/**, GET /shipments/track/**

// Admin only
hasRole(ADMIN): /admin/**
```

### Security Features
- CSRF protection enabled
- Session fixation protection
- Secure cookies (HttpOnly, Secure, SameSite)
- Password encoding (BCrypt)
- Rate limiting on login
- Input validation and sanitization

---

## Caching Strategy (Redis)

### Cached Entities

| Key Pattern | TTL | Description |
|-------------|-----|-------------|
| `product:{id}` | 15 min | Single product definition |
| `products:list:{page}:{size}` | 5 min | Paginated product list |
| `products:category:{categoryId}` | 5 min | Products by category |
| `categories:all` | 30 min | All categories |
| `stock:availability:{productId}` | 1 min | Stock availability |

### Cache Invalidation
- Product update → invalidate `product:{id}`, `products:list:*`, `products:category:*`
- Stock change → invalidate `stock:availability:{productId}`
- Category change → invalidate `categories:all`

---

## Observability

### Jaeger Tracing
- Trace HTTP requests
- Trace database queries
- Trace Redis operations
- Trace inter-module communication
- Custom spans for business operations

### Logging
- Structured JSON logging (Logback)
- Correlation IDs (trace-id, span-id)
- Log levels: ERROR for exceptions, WARN for business rule violations, INFO for state changes, DEBUG for detailed flow
- Sensitive data masking (passwords, emails partially)

### Metrics (Micrometer)
- Request latency
- Error rates
- Cache hit/miss ratio
- Database connection pool
- Active sessions

---

## Testing Strategy

### Unit Tests (TestNG) - Domain Only

**Location**: `src/test/java/com/simpleshop/{module}/domain/`

**Coverage**:
- Aggregate root business logic
- Value object validation
- Domain service logic
- Domain event creation

**Examples**:
```java
// CartTest.java
@Test
void shouldAddItemToCart()
@Test
void shouldUpdateItemQuantity()
@Test
void shouldRemoveItem()
@Test
void shouldCalculateTotal()
@Test
void shouldThrowWhenQuantityExceedsLimit()

// MoneyTest.java
@Test
void shouldAddMoneyWithSameCurrency()
@Test
void shouldThrowWhenAddingDifferentCurrencies()
@Test
void shouldMultiplyByQuantity()

// OrderTest.java
@Test
void shouldPlaceOrderWithValidItems()
@Test
void shouldRejectEmptyOrder()
@Test
void shouldTransitionToConfirmed()
@Test
void shouldNotAllowInvalidTransition()
```

**Test Configuration**:
```xml
<!-- testng.xml -->
<suite name="Domain Unit Tests">
    <test name="Catalog Domain">
        <packages>
            <package name="com.simpleshop.catalog.domain.*"/>
        </packages>
    </test>
    <!-- ... other modules ... -->
</suite>
```

### E2E Tests (JUnit 5 + Testcontainers)

**Location**: `src/test/java/com/simpleshop/e2e/`

**Infrastructure**:
- PostgreSQL Testcontainer
- Redis Testcontainer
- Mailhog Testcontainer

**Critical Happy Path Scenarios**:

```java
@Testcontainers
class PurchaseFlowE2ETest {
    
    @Test
    void anonymousUserCanBrowseProducts()
    // 1. Open home page
    // 2. View product list
    // 3. Filter by category
    // 4. View product details
    
    @Test
    void anonymousUserCanAddToCartAndRegister()
    // 1. Add products to cart
    // 2. View cart
    // 3. Register new account
    // 4. Cart is preserved after login
    
    @Test
    void authenticatedUserCanCompletePurchase()
    // 1. Login
    // 2. Add products to cart
    // 3. Checkout
    // 4. Verify order created
    // 5. Verify stock reduced
    // 6. Verify shipment created
    // 7. Verify email sent (check Mailhog)
    
    @Test
    void userCanTrackShipment()
    // 1. Complete purchase
    // 2. Get tracking link from email
    // 3. View shipment status
    // 4. Verify status history
    
    @Test
    void adminCanManageProducts()
    // 1. Login as admin
    // 2. Create product definition
    // 3. Update product
    // 4. Verify product visible to users
    // 5. Deactivate product
    // 6. Verify product hidden
}
```

### Integration Tests

**Location**: `src/test/java/com/simpleshop/{module}/infrastructure/`

**Coverage**:
- Repository adapters
- Cache adapters
- Email sender
- Module event publishing

---

## Docker Configuration

### docker-compose.yml (Development)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: simpleshop
      POSTGRES_USER: shop
      POSTGRES_PASSWORD: shop123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U shop -d simpleshop"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5

  mailhog:
    image: mailhog/mailhog:latest
    ports:
      - "1025:1025"  # SMTP
      - "8025:8025"  # Web UI
    
  jaeger:
    image: jaegertracing/all-in-one:latest
    environment:
      COLLECTOR_ZIPKIN_HOST_PORT: 9411
    ports:
      - "5775:5775/udp"
      - "6831:6831/udp"
      - "6832:6832/udp"
      - "5778:5778"
      - "16686:16686"  # Web UI
      - "14250:14250"
      - "14268:14268"
      - "14269:14269"
      - "9411:9411"

volumes:
  postgres_data:
```

### docker-compose.test.yml (Testcontainers base)

```yaml
version: '3.8'

services:
  postgres-test:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: simpleshop_test
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
    tmpfs:
      - /var/lib/postgresql/data

  redis-test:
    image: redis:7-alpine

  mailhog-test:
    image: mailhog/mailhog:latest
```

### Dockerfile (Application)

```dockerfile
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## UI Views (Thymeleaf)

### Public Pages
- `index.html` - Home page with featured products
- `products/list.html` - Product listing with filters
- `products/detail.html` - Single product view
- `cart/view.html` - Shopping cart
- `auth/login.html` - Login form
- `auth/register.html` - Registration form

### User Pages (Authenticated)
- `checkout/form.html` - Checkout with address
- `checkout/confirmation.html` - Order confirmation
- `orders/list.html` - User's orders
- `orders/detail.html` - Single order view
- `shipments/track.html` - Shipment tracking

### Admin Pages
- `admin/index.html` - Admin dashboard (simple view)
- `admin/products/list.html` - Product management list
- `admin/products/form.html` - Create/Edit product form

### Layouts
- `layouts/main.html` - Main layout with header, footer
- `layouts/admin.html` - Admin layout
- `fragments/header.html` - Navigation header
- `fragments/footer.html` - Footer
- `fragments/cart-widget.html` - Mini cart in header

---

## Implementation Phases

### Phase 1: Foundation (Week 1)
- [ ] Project setup (Spring Boot, Modulith, dependencies)
- [ ] Docker Compose configuration
- [ ] Shared kernel implementation
- [ ] Database schema and migrations (Flyway)
- [ ] Base classes for DDD (AggregateRoot, Entity, ValueObject)

### Phase 2: Identity Module (Week 1-2)
- [ ] User domain model
- [ ] Spring Security configuration
- [ ] Registration and login flows
- [ ] Role-based access control
- [ ] Session management
- [ ] Unit tests for User domain

### Phase 3: Catalog Module (Week 2)
- [ ] ProductDefinition and Category domains
- [ ] CRUD operations
- [ ] Redis caching
- [ ] Admin product management
- [ ] Unit tests for Catalog domain
- [ ] Public product browsing views

### Phase 4: Inventory Module (Week 2-3)
- [ ] Warehouse and Stock domains
- [ ] Stock reservation logic
- [ ] Event listeners for catalog events
- [ ] Unit tests for Inventory domain

### Phase 5: Cart Module (Week 3)
- [ ] Cart domain model
- [ ] Anonymous cart (session-based)
- [ ] Authenticated cart (user-based)
- [ ] Cart merging on login
- [ ] Cart views
- [ ] Unit tests for Cart domain

### Phase 6: Order Module (Week 3-4)
- [ ] Order domain model
- [ ] Checkout flow
- [ ] Order placement saga
- [ ] Order views
- [ ] Unit tests for Order domain

### Phase 7: Shipping Module (Week 4)
- [ ] Shipment domain model
- [ ] Tracking number generation
- [ ] Status tracking
- [ ] Event listeners for order events
- [ ] Tracking views
- [ ] Unit tests for Shipping domain

### Phase 8: Notification Module (Week 4-5)
- [ ] Email templates
- [ ] Mailhog integration
- [ ] Invoice generation
- [ ] Event listeners for various events
- [ ] Unit tests for Notification domain

### Phase 9: Observability (Week 5)
- [ ] Jaeger tracing configuration
- [ ] Custom spans
- [ ] Structured logging
- [ ] Micrometer metrics

### Phase 10: E2E Tests & Polish (Week 5-6)
- [ ] Testcontainers setup
- [ ] E2E test implementation
- [ ] UI polish
- [ ] Security hardening
- [ ] Performance optimization
- [ ] Documentation

---

## Configuration Files

### application.yml

```yaml
spring:
  application:
    name: simple-shop
  
  datasource:
    url: jdbc:postgresql://localhost:5432/simpleshop
    username: shop
    password: shop123
    hikari:
      maximum-pool-size: 10
  
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
  
  data:
    redis:
      host: localhost
      port: 6379
  
  mail:
    host: localhost
    port: 1025
  
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html

  modulith:
    events:
      republish-outstanding-events-on-restart: true
      
management:
  tracing:
    sampling:
      probability: 1.0
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{traceId:-}] [%X{spanId:-}] %-5level %logger{36} - %msg%n"
  level:
    com.simpleshop: DEBUG
    org.springframework.modulith: DEBUG
```

### pom.xml (key dependencies)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.0</version>
</parent>

<properties>
    <java.version>25</java.version>
</properties>

<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Spring Modulith -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-jpa</artifactId>
    </dependency>
    
    <!-- Thymeleaf Extras -->
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    
    <!-- Tracing -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-otel</artifactId>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-otlp</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.9.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-bom</artifactId>
            <version>1.3.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>1.20.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## Quality Gates

### Pre-commit
- Compile check
- Unit tests pass
- Checkstyle (code style)
- SpotBugs (static analysis)

### CI Pipeline
- All unit tests (TestNG)
- Integration tests
- E2E tests (Testcontainers)
- Modulith architecture verification
- Code coverage (>80% for domain)
- Security scan (OWASP dependency check)

### Definition of Done
- [ ] Domain logic covered by unit tests
- [ ] E2E test for happy path
- [ ] No compiler warnings
- [ ] No security vulnerabilities
- [ ] Tracing spans added
- [ ] Appropriate logging
- [ ] Documentation updated (if needed)

---

## File Structure Overview

```
simple-shop/
├── docker/
│   ├── docker-compose.yml
│   └── docker-compose.test.yml
├── src/
│   ├── main/
│   │   ├── java/com/simpleshop/
│   │   │   ├── SimpleShopApplication.java
│   │   │   ├── shared/
│   │   │   ├── catalog/
│   │   │   ├── inventory/
│   │   │   ├── cart/
│   │   │   ├── order/
│   │   │   ├── identity/
│   │   │   ├── shipping/
│   │   │   └── notification/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── db/migration/
│   │       ├── templates/
│   │       │   ├── layouts/
│   │       │   ├── fragments/
│   │       │   ├── products/
│   │       │   ├── cart/
│   │       │   ├── checkout/
│   │       │   ├── orders/
│   │       │   ├── shipments/
│   │       │   ├── auth/
│   │       │   ├── admin/
│   │       │   └── email/
│   │       └── static/
│   │           ├── css/
│   │           └── js/
│   └── test/
│       ├── java/com/simpleshop/
│       │   ├── catalog/domain/
│       │   ├── inventory/domain/
│       │   ├── cart/domain/
│       │   ├── order/domain/
│       │   ├── identity/domain/
│       │   ├── shipping/domain/
│       │   ├── notification/domain/
│       │   ├── e2e/
│       │   └── ModulithArchitectureTest.java
│       └── resources/
│           ├── application-test.yml
│           └── testng.xml
├── Dockerfile
├── pom.xml
├── README.md
└── plan.md
```
