package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.AdminOrderPage;
import com.simpleshop.e2e.pages.ShipmentTrackingPage;
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

class ShipmentProgressionE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private AdminOrderPage adminOrderPage;
    private ShipmentTrackingPage shipmentTrackingPage;

    private static final String ADMIN_EMAIL = "admin-ship@test.com";
    private static final String ADMIN_PASSWORD = "Password123!";
    private static final String USER_EMAIL = "user-ship@test.com";
    private static final String USER_PASSWORD = "Password123!";

    private UUID orderId;
    private UUID productId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        fixture.cleanAll();
        fixture.createUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin", "Ship", "ADMIN");
        UUID userId = fixture.createUser(USER_EMAIL, USER_PASSWORD, "Ship", "User", "USER");

        UUID categoryId = fixture.createCategory("Furniture");
        productId = fixture.createProduct("Office Chair", "SKU-CHAIR-001",
                new BigDecimal("299.99"), "USD", categoryId, true);
        warehouseId = fixture.createWarehouse("Furniture Warehouse", "100 Furn Ave", "Furntown", "40401", "US");
        fixture.addStock(productId, warehouseId, 50);

        orderId = createOrder(userId, productId, warehouseId);
        confirmOrderAndWaitForShipments();

        adminOrderPage = new AdminOrderPage(page);
        shipmentTrackingPage = new ShipmentTrackingPage(page);
    }

    @Test
    void adminProgressesShipmentThroughAllStatuses() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders/" + orderId);

        List<String> trackingNumbers = adminOrderPage.getShipmentTrackingNumbers();
        assertThat(trackingNumbers).isNotEmpty();
        String trackingNumber = trackingNumbers.getFirst();

        String[] statuses = {"PICKED", "PACKED", "SHIPPED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"};
        for (String status : statuses) {
            adminOrderPage.updateShipmentStatus(trackingNumber, status);
            navigateTo("/admin/orders/" + orderId);
        }

        navigateTo("/shipments/track/" + trackingNumber);
        assertThat(shipmentTrackingPage.getCurrentStatus()).containsIgnoringCase("Delivered");
    }

    @Test
    void customerTrackingShowsStatusHistory() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders/" + orderId);

        List<String> trackingNumbers = adminOrderPage.getShipmentTrackingNumbers();
        String trackingNumber = trackingNumbers.getFirst();

        adminOrderPage.updateShipmentStatus(trackingNumber, "PICKED");
        navigateTo("/admin/orders/" + orderId);
        adminOrderPage.updateShipmentStatus(trackingNumber, "PACKED");
        navigateTo("/admin/orders/" + orderId);
        adminOrderPage.updateShipmentStatus(trackingNumber, "SHIPPED");

        loginAs(USER_EMAIL, USER_PASSWORD);
        navigateTo("/shipments/track/" + trackingNumber);

        assertThat(shipmentTrackingPage.getCurrentStatus()).containsIgnoringCase("Shipped");
        List<String> history = shipmentTrackingPage.getStatusHistoryEntries();
        assertThat(history).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void deliveredShipment_furtherUpdatesBlocked() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders/" + orderId);

        List<String> trackingNumbers = adminOrderPage.getShipmentTrackingNumbers();
        String trackingNumber = trackingNumbers.getFirst();

        String[] statuses = {"PICKED", "PACKED", "SHIPPED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"};
        for (String status : statuses) {
            adminOrderPage.updateShipmentStatus(trackingNumber, status);
            navigateTo("/admin/orders/" + orderId);
        }

        boolean hasUpdateForm = page.locator("form[action*='shipments'] select[name='newStatus']").isVisible();
        assertThat(hasUpdateForm).isFalse();
    }

    private UUID createOrder(UUID userId, UUID productId, UUID warehouseId) {
        UUID id = UUID.randomUUID();
        String orderNumber = "ORD-" + System.currentTimeMillis();
        Timestamp now = Timestamp.from(Instant.now());

        jdbcTemplate.update(
                "INSERT INTO orders (id, user_id, order_number, status, total_amount, total_currency, " +
                        "shipping_street, shipping_city, shipping_postal_code, shipping_country, created_at) " +
                        "VALUES (?, ?, ?, 'PENDING', ?, 'USD', ?, ?, ?, ?, ?)",
                id, userId, orderNumber, new BigDecimal("299.99"),
                "100 Furn Ave", "Furntown", "40401", "US", now
        );

        UUID itemId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO order_items (id, order_id, product_id, product_name, quantity, " +
                        "unit_price_amount, unit_price_currency, warehouse_id) " +
                        "VALUES (?, ?, ?, 'Office Chair', 1, ?, 'USD', ?)",
                itemId, id, productId, new BigDecimal("299.99"), warehouseId
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
