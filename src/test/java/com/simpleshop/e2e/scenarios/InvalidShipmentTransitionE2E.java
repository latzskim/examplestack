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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class InvalidShipmentTransitionE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private AdminOrderPage adminOrderPage;

    private static final String ADMIN_EMAIL = "admin-invalid@test.com";
    private static final String ADMIN_PASSWORD = "Password123!";

    private UUID orderId;

    @BeforeEach
    void setUp() {
        fixture.cleanAll();
        fixture.createUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin", "Invalid", "ADMIN");
        UUID userId = fixture.createUser("user-invalid@test.com", "Password123!", "Invalid", "User", "USER");

        UUID categoryId = fixture.createCategory("Tools");
        UUID productId = fixture.createProduct("Hammer", "SKU-HAMMER-001",
                new BigDecimal("39.99"), "USD", categoryId, true);
        UUID warehouseId = fixture.createWarehouse("Tool Warehouse", "200 Tool Rd", "Toolville", "50501", "US");
        fixture.addStock(productId, warehouseId, 20);

        orderId = createOrder(userId, productId, warehouseId);
        confirmOrderAndWaitForShipments();

        adminOrderPage = new AdminOrderPage(page);
    }

    @Test
    void invalidTransition_createdToDelivered_isRejected() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders/" + orderId);

        List<String> trackingNumbers = adminOrderPage.getShipmentTrackingNumbers();
        assertThat(trackingNumbers).isNotEmpty();
        String trackingNumber = trackingNumbers.getFirst();

        adminOrderPage.updateShipmentStatus(trackingNumber, "DELIVERED");

        assertThat(adminOrderPage.getErrorMessage()).isNotEmpty();
    }

    @Test
    void invalidTransition_shipmentStatusUnchanged() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders/" + orderId);

        List<String> trackingNumbers = adminOrderPage.getShipmentTrackingNumbers();
        String trackingNumber = trackingNumbers.getFirst();

        adminOrderPage.updateShipmentStatus(trackingNumber, "DELIVERED");

        navigateTo("/shipments/track/" + trackingNumber);
        String currentStatus = page.locator(".card-header .badge.fs-6").textContent().trim();
        assertThat(currentStatus).containsIgnoringCase("Created");
    }

    private UUID createOrder(UUID userId, UUID productId, UUID warehouseId) {
        UUID id = UUID.randomUUID();
        String orderNumber = "ORD-" + System.currentTimeMillis();
        Timestamp now = Timestamp.from(Instant.now());

        jdbcTemplate.update(
                "INSERT INTO orders (id, user_id, order_number, status, total_amount, total_currency, " +
                        "shipping_street, shipping_city, shipping_postal_code, shipping_country, created_at) " +
                        "VALUES (?, ?, ?, 'PENDING', ?, 'USD', ?, ?, ?, ?, ?)",
                id, userId, orderNumber, new BigDecimal("39.99"),
                "200 Tool Rd", "Toolville", "50501", "US", now
        );

        UUID itemId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO order_items (id, order_id, product_id, product_name, quantity, " +
                        "unit_price_amount, unit_price_currency, warehouse_id) " +
                        "VALUES (?, ?, ?, 'Hammer', 1, ?, 'USD', ?)",
                itemId, id, productId, new BigDecimal("39.99"), warehouseId
        );

        return id;
    }

    private void confirmOrderAndWaitForShipments() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders/" + orderId);
        AdminOrderPage setupPage = new AdminOrderPage(page);
        setupPage.clickPaymentSuccess();
        await().atMost(10, TimeUnit.SECONDS).until(() -> fixture.countShipments(orderId) > 0);
    }
}
