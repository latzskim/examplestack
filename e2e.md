# E2E Test Scenarios for Simple Shop

## 1. Scope and goals
This document defines critical end-to-end scenarios for the current application behavior (catalog, cart, identity, checkout, order lifecycle, inventory, shipping, notifications, and admin operations).

Goals:
- Validate the highest-risk business paths, not every UI detail.
- Cover both happy paths and critical failure/authorization paths.
- Ensure cross-module orchestration is correct (order -> inventory -> shipping -> notification).

## 2. E2E testing strategy for this project

### 2.1 Prioritization model
- P0: Revenue/security critical. Must pass on every PR to main.
- P1: Operational/admin critical. Must pass at least nightly and before release.
- P2: Nice-to-have UX/regression checks. Optional in PR, required for release candidate.

### 2.2 Test design principles
- Use business outcomes as assertions (order status, stock numbers, shipment creation, notification log), not only UI text.
- Keep one business concern per scenario where possible.
- Use stable selectors (`data-testid`) for future implementation; avoid brittle CSS/xpath tied to style.
- Make each scenario independent: isolated test data and deterministic setup.
- Prefer deterministic backend state setup via DB/API fixture helpers over long UI setup for every test.

### 2.3 Environment assumptions
- App runs with production-like profile (security and module listeners enabled).
- Test infrastructure includes PostgreSQL, Redis, and MailHog.
- Time-sensitive checks (timestamps, sorting) should use tolerant assertions (ranges), not exact instant equality.

## 3. Critical scenarios

---

### Scenario P0-01: Anonymous browsing and category filtering
Priority: P0

Objective:
Validate that a visitor can discover products and reach product detail pages, and only active products are visible.

Preconditions:
- At least 2 active products in different categories.
- At least 1 inactive product exists.

Steps:
1. Open `/products`.
2. Verify product list renders with active products.
3. Apply category filter from sidebar.
4. Open a product detail page from filtered list.
5. Navigate directly to inactive product detail URL.

Expected results:
- Product list shows only active products.
- Filtering updates list to selected category.
- Product detail shows name/price/SKU/stock state.
- Inactive product detail access redirects to `/products`.

---

### Scenario P0-02: Anonymous cart management
Priority: P0

Objective:
Verify core cart operations for anonymous sessions.

Preconditions:
- One active product with stock > 10.

Steps:
1. Open product detail page.
2. Add quantity 2 to cart.
3. Open `/cart`.
4. Update quantity to 3.
5. Remove item.
6. Re-add item and then clear cart.

Expected results:
- Cart reflects add/update/remove/clear operations correctly.
- Subtotal and total calculations match quantity * unit price.
- Empty cart state is shown after remove/clear.
- Session cart persists between page navigations before login.

---

### Scenario P0-03: Registration validation and successful login
Priority: P0

Objective:
Validate registration constraints and login flow.

Preconditions:
- No existing user for `new-user@example.com`.

Steps:
1. Open `/register`.
2. Submit empty email.
3. Submit password shorter than 8 chars.
4. Submit valid registration.
5. Submit same email again.
6. Login with created credentials via `/login`.

Expected results:
- Empty email and short password show validation errors.
- Valid registration redirects to login with success message.
- Duplicate registration shows "Email already registered".
- Login succeeds and user session is established.

---

### Scenario P0-04: Cart merge after login
Priority: P0

Objective:
Verify anonymous cart is merged into user cart on authentication success.

Preconditions:
- Anonymous session has product A qty 2.
- Existing user cart has product A qty 1 and product B qty 1.

Steps:
1. As anonymous user, ensure cart contains product A qty 2.
2. Login with existing user account.
3. Open `/cart`.

Expected results:
- Cart contains merged items.
- Product A quantity becomes 3 (sum of anonymous + user cart).
- Product B remains present.
- Session cart id is removed and anonymous cart no longer exists.

---

### Scenario P0-05: Checkout guard and redirect behavior
Priority: P0

Objective:
Ensure checkout is protected and empty-cart checkout is blocked.

Preconditions:
- Anonymous user with non-empty cart.
- Authenticated user with empty cart.

Steps:
1. As anonymous user open `/checkout`.
2. Login and return.
3. As authenticated user with empty cart open `/checkout`.

Expected results:
- Anonymous request is redirected to login.
- After authentication, checkout form is accessible.
- Empty cart checkout redirects to `/cart`.

---

### Scenario P0-06: Place order from cart (happy path)
Priority: P0

Objective:
Validate end-to-end purchase creation from cart with stock reservation and order creation.

Preconditions:
- Authenticated user.
- Cart has at least one item.
- Sufficient stock exists in inventory.

Steps:
1. Open checkout form and submit valid shipping address.
2. Complete checkout.
3. Open confirmation page and then `/orders` and order detail.

Expected results:
- Order is created with status `PENDING`.
- Order number is generated and shown.
- Ordered items, totals, and shipping address are persisted correctly.
- Cart is cleared after successful order creation.
- Reserved stock increases (available decreases) for ordered items.

---

### Scenario P0-07: Checkout failure when stock is insufficient
Priority: P0

Objective:
Ensure purchase fails safely when requested quantity cannot be allocated.

Preconditions:
- Authenticated user cart has product quantity > available stock.

Steps:
1. Open checkout.
2. Submit shipping form.

Expected results:
- User is redirected back to checkout with error message.
- No order is created.
- Cart content remains unchanged.
- No shipment is created.

---

### Scenario P0-08: User order access control
Priority: P0

Objective:
Ensure users cannot access or cancel orders that do not belong to them.

Preconditions:
- User A has an order.
- User B exists.

Steps:
1. Login as User B.
2. Open `/orders/{userAOrderId}`.
3. Attempt `POST /orders/{userAOrderId}/cancel`.

Expected results:
- Access is denied (error/redirect behavior per current controller handling).
- Order state for User A remains unchanged.
- No side effects (no cancellation, no stock release).

---

### Scenario P1-01: Admin product lifecycle impacts storefront
Priority: P1

Objective:
Verify admin can manage product visibility and storefront reflects it.

Preconditions:
- Admin account available.

Steps:
1. Login as admin.
2. Create new product.
3. Verify it appears in `/admin/products` and `/products`.
4. Deactivate product.
5. Verify it disappears from `/products` and direct product URL redirects.
6. Activate product again and verify it reappears.

Expected results:
- Product CRUD actions succeed.
- Active flag directly controls storefront visibility.

---

### Scenario P1-02: Admin inventory replenish and availability propagation
Priority: P1

Objective:
Validate stock management from admin panel affects customer purchase ability.

Preconditions:
- Product exists with zero or low stock.
- Active warehouse exists.

Steps:
1. Open `/admin/inventory/products/{productId}`.
2. Replenish stock for selected warehouse.
3. Open product detail page as user.
4. Add to cart and proceed toward checkout.

Expected results:
- Replenishment increases available stock.
- Product detail reflects updated stock availability.
- Checkout path works when sufficient stock exists.

---

### Scenario P1-03: Admin payment success flow (order confirmation orchestration)
Priority: P1

Objective:
Validate payment-success action triggers full cross-module workflow.

Preconditions:
- Existing `PENDING` order with reserved stock.

Steps:
1. As admin open `/admin/orders/{orderId}`.
2. Trigger "Payment Success".
3. Refresh order detail and related shipment tracking.
4. Inspect notification logs (DB) and MailHog inbox.

Expected results:
- Order transitions to `CONFIRMED`.
- Reserved stock is confirmed (deducted from total quantity).
- One shipment per warehouse is created.
- Notification attempts are logged for order confirmation and invoice; shipment-created notification is logged when shipment event fires.
- Customer can see shipment section in order detail.

---

### Scenario P1-04: Admin payment failure flow (cancellation and stock release)
Priority: P1

Objective:
Ensure payment failure cancels order and releases reservations safely.

Preconditions:
- Existing `PENDING` order with reserved stock.

Steps:
1. As admin open order detail.
2. Trigger "Payment Failed".
3. Verify order and inventory state.

Expected results:
- Order transitions to `CANCELLED` with cancellation reason.
- Reserved quantities are released.
- No new shipment is created.
- Customer sees cancelled state in `/orders/{id}`.

---

### Scenario P1-05: Shipment status progression and public tracking
Priority: P1

Objective:
Validate shipment lifecycle updates and customer tracking timeline.

Preconditions:
- Confirmed order with created shipment.

Steps:
1. As admin, move shipment through valid statuses: `PICKED` -> `PACKED` -> `SHIPPED` -> `IN_TRANSIT` -> `OUT_FOR_DELIVERY` -> `DELIVERED`.
2. Open `/shipments/track/{trackingNumber}` as authenticated user.

Expected results:
- Each valid transition succeeds.
- Tracking page shows current status and ordered status history entries.
- Important status updates (`SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`) generate notification log entries and emails.
- Once `DELIVERED`, further status updates are blocked.

---

### Scenario P1-06: Invalid shipment transition rejection
Priority: P1

Objective:
Ensure shipment state machine prevents illegal transitions.

Preconditions:
- Shipment in `CREATED`.

Steps:
1. Attempt direct update from `CREATED` to `DELIVERED`.

Expected results:
- Update is rejected with error feedback.
- Shipment status remains unchanged.
- No misleading notifications are sent.

---

### Scenario P1-07: Role and endpoint protection
Priority: P1

Objective:
Verify route protection matches configured security rules.

Preconditions:
- Anonymous, user, and admin accounts available.

Steps:
1. Anonymous user accesses `/admin/products` and `/orders`.
2. Regular user accesses `/admin/orders`.
3. Admin accesses `/admin/orders`.

Expected results:
- Anonymous access to protected routes is redirected to login.
- Regular user is denied access to admin routes.
- Admin can access admin routes.
- Public routes (`/products`, `/cart`, `/login`, `/register`) remain accessible anonymously.

## 4. Technical implementation documentation

### 4.1 Recommended framework stack
Use:
- Playwright (Java) for browser automation.
- JUnit 5 for test runner and assertions.
- Testcontainers for environment provisioning (PostgreSQL, Redis, MailHog).

Why this stack:
- Project already uses JUnit 5 + Testcontainers in integration tests.
- Playwright is more stable than Selenium in modern SPA-like/async UIs (auto-waits, robust browser control, tracing/screenshots/videos).
- Java Playwright keeps language/tooling consistent with the backend.
- Testcontainers gives reproducible infra in CI and local runs.

### 4.2 Proposed test architecture
Suggested package layout:
- `src/test/java/com/simpleshop/e2e/config` (containers, app bootstrap, mail helper)
- `src/test/java/com/simpleshop/e2e/fixtures` (data builders/seeding helpers)
- `src/test/java/com/simpleshop/e2e/pages` (page objects with stable selectors)
- `src/test/java/com/simpleshop/e2e/scenarios` (scenario-focused tests)

Key design:
- One base test class starts containers and app once per suite.
- Before each test, reset DB/mail state or generate unique tenant-like data prefixes.
- Scenario classes should read like business flows.

### 4.3 Test data and isolation strategy
- Use deterministic fixture factories for users/products/stock/orders.
- Use unique emails/order references per test run to avoid collisions.
- Keep scenario setup via API/DB helper where possible; only validate behavior via UI.
- Do not chain scenario dependencies.

### 4.4 Assertions strategy
Each scenario should include:
- UI assertions (visible text/status/buttons).
- Backend assertions (DB state for orders, stocks, shipments, notification_logs).
- External assertions (MailHog message count/content when notification is expected).

### 4.5 CI execution strategy
- PR pipeline: run all P0 scenarios.
- Nightly/release pipeline: run P0 + P1.
- Persist Playwright artifacts on failure (trace, screenshot, video) for debugging.
- Parallelize scenarios by class, but keep shared resources isolated.

### 4.6 Practical implementation steps
1. Add Playwright Java dependencies and JUnit integration to `pom.xml` test scope.
2. Create E2E base test with Testcontainers for PostgreSQL/Redis/MailHog.
3. Implement fixture layer for creating users, categories, products, warehouses, stock.
4. Add `data-testid` attributes in templates to stabilize selectors.
5. Implement page objects for auth, products, cart, checkout, orders, admin orders, shipment tracking.
6. Implement scenarios in priority order: all P0 first, then P1.
7. Add CI matrix/jobs for P0 and full suite.

## 5. Notes specific to this codebase
- `plan.md` includes target architecture and sample E2E direction, but scenarios above align to currently implemented controllers/domain rules.
- Current security config permits `/shipments/track/**` only for authenticated users; keep tests aligned with this behavior.
- Shipment creation is event-driven on `OrderConfirmed`, so tests must wait/assert asynchronously where needed.
