package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.AdminOrderPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class AdminPaymentFailureE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private AdminOrderPage adminOrderPage;

    private static final String ADMIN_EMAIL = "admin-fail@test.com";
    private static final String ADMIN_PASSWORD = "Password123!";

    private UUID userId;
    private UUID orderId;
    private UUID productId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        fixture.cleanAll();
        fixture.createUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin", "Fail", "ADMIN");
        userId = fixture.createUser("user-fail@test.com", "Password123!", "Fail", "User", "USER");

        UUID categoryId = fixture.createCategory("Toys");
        productId = fixture.createProduct("Toy Car", "SKU-TOY-001",
                new BigDecimal("19.99"), "USD", categoryId, true);
        warehouseId = fixture.createWarehouse("Toy Warehouse", "789 Toy Ln", "Toytown", "30301", "US");
        fixture.addStock(productId, warehouseId, 10);

        orderId = createOrder(userId, productId, warehouseId);

        adminOrderPage = new AdminOrderPage(page);
    }

    @Test
    void paymentFailed_orderBecomesCancelled() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/orders/" + orderId);
        assertThat(adminOrderPage.getOrderStatus()).containsIgnoringCase("PENDING");

        adminOrderPage.clickPaymentFailed();

        assertThat(adminOrderPage.getOrderStatus()).containsIgnoringCase("CANCELLED");
        assertThat(fixture.getOrderStatus(orderId)).isEqualTo("CANCELLED");
    }

    @Test
    void paymentFailed_releasesReservedStock() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/orders/" + orderId);
        adminOrderPage.clickPaymentFailed();

        await().atMost(10, TimeUnit.SECONDS).until(() -> fixture.getReservedStock(productId) == 0);

        assertThat(fixture.getReservedStock(productId)).isZero();
    }

    @Test
    void paymentFailed_noShipmentsCreated() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/orders/" + orderId);
        adminOrderPage.clickPaymentFailed();

        assertThat(fixture.countShipments(orderId)).isZero();
    }

    private UUID createOrder(UUID userId, UUID productId, UUID warehouseId) {
        UUID id = UUID.randomUUID();
        String orderNumber = "ORD-" + System.currentTimeMillis();
        Timestamp now = Timestamp.from(Instant.now());

        jdbcTemplate.update(
                "INSERT INTO orders (id, user_id, order_number, status, total_amount, total_currency, " +
                        "shipping_street, shipping_city, shipping_postal_code, shipping_country, created_at) " +
                        "VALUES (?, ?, ?, 'PENDING', ?, 'USD', ?, ?, ?, ?, ?)",
                id, userId, orderNumber, new BigDecimal("19.99"),
                "123 Ship St", "Shiptown", "90210", "US", now
        );

        UUID itemId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO order_items (id, order_id, product_id, product_name, quantity, " +
                        "unit_price_amount, unit_price_currency, warehouse_id) " +
                        "VALUES (?, ?, ?, 'Toy Car', 1, ?, 'USD', ?)",
                itemId, id, productId, new BigDecimal("19.99"), warehouseId
        );

        return id;
    }
}
