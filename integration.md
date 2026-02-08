# Backend Integration Test Plan for Simple Shop

## 1. Purpose
This document defines backend integration tests for the current codebase. It focuses on module boundaries, persistence queries, transactional orchestration, security behavior, and event-driven side effects.

This is not a unit-test list and not a UI E2E plan.

## 2. What integration tests mean in this project
Integration tests should verify that real components work together:
- Spring context wiring (services, adapters, listeners).
- JPA mappings and custom queries against PostgreSQL.
- Transaction behavior across service boundaries.
- Security filters and route authorization using MockMvc.
- Event-driven module interactions (order -> inventory/shipping/notification).
- External adapter contracts (mail/PDF) at application boundary level.

## 3. Decision model: what to test vs not test

### 3.1 Must be integration-tested
Test when at least one is true:
- Custom query exists (`@Query`, sorting/filtering/pagination logic).
- Transactional multi-step flow can partially fail.
- Cross-module side effects depend on domain events/listeners.
- Security rules/CSRF/session behavior influence correctness.
- Adapter maps app data to external format (email/template/PDF).

### 3.2 Should NOT be integration-tested
Do not integration-test when:
- Behavior is pure domain logic already covered by unit tests.
- Code is trivial pass-through with no query/transform/rule.
- Assertion would duplicate framework internals (Spring/JPA itself).
- Scenario is purely visual/UI layout behavior.
- Scenario needs high cardinality permutations better suited to unit tests.

## 4. Prioritized integration test scenarios

---

## A. Persistence and repository integration (P0)

### INT-P0-01: Product filtering query correctness
Priority: P0

Why:
`ProductJpaRepository.findAllWithFilters(...)` contains custom query logic for `categoryId` and `activeOnly`.

Preconditions:
- Products in multiple categories; mix of active/inactive.

Steps:
1. Query with `categoryId = null, activeOnly = true`.
2. Query with specific category and `activeOnly = true`.
3. Query with specific category and `activeOnly = false`.
4. Validate pagination metadata and content ordering stability.

Expected:
- Active-only filtering works.
- Category filtering works with/without active filter.
- No inactive leakage when `activeOnly=true`.

---

### INT-P0-02: Cart repository fetch with items
Priority: P0

Why:
`LEFT JOIN FETCH` queries in cart repository are critical for cart/session/user behavior and merge.

Preconditions:
- One session cart and one user cart with multiple items.

Steps:
1. Load by session id.
2. Load by user id.
3. Load by cart id.
4. Access items after session to confirm eager fetch intent.

Expected:
- Correct cart found per selector.
- Associated items are present and complete.
- No duplicate/missing item rows from join behavior.

---

### INT-P0-03: Stock aggregation queries
Priority: P0

Why:
Availability in storefront and allocation depends on `sumAvailableByProductId` and `sumReservedByProductId`.

Preconditions:
- Same product stocked in at least 2 warehouses with different reserved quantities.

Steps:
1. Persist stock rows with known `quantity/reserved` values.
2. Call sum available and reserved methods.

Expected:
- `available = sum(quantity - reserved)`.
- `reserved = sum(reserved)`.
- Missing product returns 0, not null.

---

### INT-P0-04: Order repository lookup and ordering
Priority: P0

Why:
User order history and admin order list depend on repository sort semantics.

Preconditions:
- Multiple orders across users with distinct timestamps.

Steps:
1. Query by order number.
2. Query by user pageable.
3. Query all pageable.

Expected:
- Lookup by order number is exact.
- User list ordered by `createdAt desc`.
- Global list ordered by `createdAt desc`.

---

### INT-P0-05: Shipment repository tracking and order-scoped listing
Priority: P0

Why:
Tracking and order detail shipment sections depend on tracking number lookup + order list sorting.

Steps:
1. Persist multiple shipments for same order at different creation times.
2. Query by tracking number.
3. Query by order id pageable.

Expected:
- Tracking lookup returns exact shipment.
- Order shipments sorted by `createdAt desc`.

---

### INT-P0-06: Sequence-based number generators
Priority: P0

Why:
Order/tracking identifiers rely on DB sequences and specific formatting.

Steps:
1. Start context and call `generate()` repeatedly for order number generator.
2. Repeat for tracking number generator.

Expected:
- Sequence is created if missing.
- Generated values are unique and monotonic.
- Format matches expected pattern (`ORD-<year>-<nnnnn>`, `SHIP-<year>-<nnnnn>`).

---

## B. Service orchestration integration (P0/P1)

### INT-P0-07: Place order from cart commits all state changes
Priority: P0

Why:
Main purchase flow spans cart + inventory + order in one transactional path.

Preconditions:
- User cart exists with items.
- Sufficient stock for all cart items.

Steps:
1. Execute `PlaceOrderFromCartUseCase`.
2. Fetch persisted order.
3. Fetch cart and stock rows.

Expected:
- Order persisted with `PENDING`.
- Stock reserved quantities increased for allocated warehouses.
- Cart cleared after successful order creation.

---

### INT-P0-08: Place order rollback on insufficient stock
Priority: P0

Why:
Failure path must not leave partial reservations/order rows.

Preconditions:
- Cart has at least two items; second item insufficient.

Steps:
1. Execute `PlaceOrderFromCartUseCase` expecting `InsufficientStockException`.
2. Re-query orders, cart, and stock.

Expected:
- No order persisted.
- No reservation side effects persisted (transaction rollback).
- Cart remains unchanged.

---

### INT-P1-01: Order confirmation triggers stock confirmation + shipment creation
Priority: P1

Why:
`OrderConfirmed` listeners coordinate inventory deduction and shipping creation.

Preconditions:
- Existing `PENDING` order with reserved stock and warehouse allocations.

Steps:
1. Execute `ConfirmOrderUseCase`.
2. Await listener completion (if async) using Awaitility.
3. Verify stock rows and shipment rows.

Expected:
- Order becomes `CONFIRMED`.
- Reserved quantities are confirmed (deducted from quantity/reserved).
- Shipments created per warehouse in order items.

---

### INT-P1-02: Order cancellation releases reserved stock
Priority: P1

Why:
`OrderCancelled` listener should reverse reservations.

Preconditions:
- `PENDING` order with reserved stock.

Steps:
1. Execute `CancelOrderUseCase`.
2. Verify stock reservations and order state.

Expected:
- Order `CANCELLED` with reason.
- Reserved stock released for all allocated items.

---

### INT-P1-03: Notification flow on order confirmed
Priority: P1

Why:
Order confirmation should generate notification attempts and logs (order confirmation + invoice).

Preconditions:
- Confirmable order and resolvable user/email.

Steps:
1. Confirm order.
2. Verify notification logs table entries.
3. Optionally verify SMTP sink received mails.

Expected:
- Notification log entries created with expected types and statuses (`SENT` or `FAILED` with error).
- No unhandled exception breaks order confirmation transaction.

---

### INT-P1-04: Shipment status notification gating
Priority: P1

Why:
Notifications should be sent only for selected statuses (`SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `FAILED`).

Preconditions:
- Shipment exists and linked user/order resolvable.

Steps:
1. Update status to non-notify transition (e.g., `PICKED`) and verify no new log.
2. Update to notify transition (e.g., `SHIPPED`) and verify log created.

Expected:
- Notification logs created only for configured important statuses.

---

## C. Web/security integration (P0/P1)

### INT-P0-09: Route authorization matrix via MockMvc
Priority: P0

Why:
Security config is central and easy to regress.

Steps:
1. Anonymous GET `/products` and `/cart`.
2. Anonymous GET `/orders` and `/admin/orders`.
3. Authenticated USER GET `/orders` and `/admin/orders`.
4. Authenticated ADMIN GET `/admin/orders`.

Expected:
- Public routes return 200 for anonymous.
- Protected user/admin routes redirect/deny as configured.
- Admin route accessible only to admin role.

---

### INT-P0-10: CSRF enforcement on state-changing endpoints
Priority: P0

Why:
Many critical routes are `POST` and CSRF protection is enabled.

Steps:
1. POST to representative endpoints without CSRF (`/cart/add`, `/checkout`, `/admin/orders/{id}/confirm`).
2. Repeat with valid CSRF token.

Expected:
- Missing token requests are rejected.
- Valid token requests pass authorization layer.

---

### INT-P1-05: Login success cart merge integration
Priority: P1

Why:
`CartMergeAuthenticationSuccessHandler` is critical session-to-user state transfer logic.

Steps:
1. Prepare anonymous session with cart and existing user cart.
2. Execute form login.
3. Verify merged cart quantities and removed session cart reference.

Expected:
- Merge occurs on successful authentication.
- Session cart id is removed from session.

---

## D. Adapter boundary integration (P1)

### INT-P1-06: Email sender template rendering and SMTP delivery
Priority: P1

Why:
`SpringMailEmailSender` maps notification type -> template and sends MIME mail.

Steps:
1. Send each notification type with minimal template data to local SMTP sink.
2. Inspect captured messages.

Expected:
- Correct subject/to/from populated.
- HTML body rendered from expected template.
- No template resolution failures for supported types.

---

### INT-P1-07: Invoice generator output contract
Priority: P1

Why:
Invoice generation is a business-facing artifact and potential failure point.

Steps:
1. Call `SimpleInvoiceGenerator.generateInvoice(orderView)` with representative order.
2. Validate output bytes.

Expected:
- Non-empty byte array.
- PDF signature present (`%PDF`).
- Contains key order metadata (order number/total) when parsed.

## 5. What should not be integration-tested in this project
- Domain invariants already covered by existing TestNG unit tests (`cart/domain`, `order/domain`, etc.).
- Simple adapter pass-through methods with no custom query or transformation.
- Static Thymeleaf presentation details and CSS behavior.
- Internal behavior of Spring Security/JPA/Testcontainers frameworks.
- Exhaustive combinatorics of value-object validation (belongs in unit tests).
- Performance/load characteristics (separate non-functional test suite).

## 6. Implementation blueprint

### 6.1 Test types and placement
Use three backend integration test layers:
- `@DataJpaTest` for repository/query correctness.
- `@SpringBootTest` for orchestration/event/transaction scenarios.
- `@SpringBootTest + @AutoConfigureMockMvc` for security and HTTP integration without browser.

Suggested structure:
- `src/test/java/com/simpleshop/integration/persistence/...`
- `src/test/java/com/simpleshop/integration/orchestration/...`
- `src/test/java/com/simpleshop/integration/web/...`
- `src/test/java/com/simpleshop/integration/support/...`

### 6.2 Infrastructure setup
Use Testcontainers for:
- PostgreSQL (required).
- Redis (if cache/redis-backed features are introduced in tests).
- MailHog (for SMTP assertions) or GreenMail.

Use `@DynamicPropertySource` in a shared base class to inject container URLs.

### 6.3 Data setup strategy
- Prefer fixture builders/factories over raw SQL for readability.
- Use helper methods for repeated setup (user/product/warehouse/stock/order).
- Keep each test independent and idempotent.
- Use transactional rollback for slice tests; explicit cleanup for full-context tests when needed.

### 6.4 Assertions strategy
For each scenario assert at least two levels when relevant:
- Business/API level (returned view/status/exception).
- Persistence side effects (orders/stocks/shipments/notification_logs).

For event-driven behavior, use Awaitility with bounded timeouts to avoid flaky sleeps.

### 6.5 Reliability practices
- No shared mutable test state across classes.
- Deterministic timestamps/IDs where possible.
- Avoid real external network dependencies.
- Keep integration suite runtime bounded by separating P0 and P1 groups/tags.

### 6.6 CI strategy
- PR pipeline: run all P0 integration tests.
- Nightly/pre-release: run full P0 + P1 integration suite.
- Publish logs and SQL on failure; keep container logs as artifacts.

## 7. Good practices for integration tests
1. Test contracts at seams, not implementation details.
2. Keep tests small and explicit: one business risk per test.
3. Use production-like dependencies (real Postgres, real transaction boundaries).
4. Assert both success path and high-risk failure path.
5. Design for debuggability: clear naming, stable fixtures, rich failure messages.
6. Control flakiness: await conditions, avoid timing assumptions, avoid shared state.
7. Keep an explicit "out of scope" list to prevent suite bloat.

## 8. How this scope was decided (what needs testing and what not)
The selection is risk-based and architecture-based:
- Highest risk: custom queries, transaction orchestration, security rules, event listeners with side effects.
- Medium risk: adapter boundaries that generate external artifacts (email, PDF).
- Lower risk: pure domain logic and trivial delegations already covered or better covered by unit tests.

In short, this plan tests places where bugs are most likely to occur in production and most expensive to detect late: persistence semantics, state transitions across modules, and authorization boundaries.
