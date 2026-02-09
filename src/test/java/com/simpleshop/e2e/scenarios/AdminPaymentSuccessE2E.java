package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.AdminOrderPage;
import com.simpleshop.e2e.pages.OrderDetailPage;
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

class AdminPaymentSuccessE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private AdminOrderPage adminOrderPage;
    private OrderDetailPage orderDetailPage;

    private static final String ADMIN_EMAIL = "admin-pay@test.com";
    private static final String ADMIN_PASSWORD = "Password123!";
    private static final String USER_EMAIL = "user-pay@test.com";
    private static final String USER_PASSWORD = "Password123!";

    private UUID userId;
    private UUID orderId;
    private UUID productId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        fixture.cleanAll();
        fixture.createUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin", "Pay", "ADMIN");
        userId = fixture.createUser(USER_EMAIL, USER_PASSWORD, "Pay", "User", "USER");

        UUID categoryId = fixture.createCategory("Books");
        productId = fixture.createProduct("Test Book", "SKU-BOOK-001",
                new BigDecimal("29.99"), "USD", categoryId, true);
        warehouseId = fixture.createWarehouse("Book Warehouse", "456 Book St", "Bookville", "10001", "US");
        fixture.addStock(productId, warehouseId, 100);

        orderId = createOrder(userId, productId, warehouseId);

        adminOrderPage = new AdminOrderPage(page);
        orderDetailPage = new OrderDetailPage(page);
    }

    @Test
    void adminConfirmsPayment_orderBecomesConfirmed() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/orders/" + orderId);
        assertThat(adminOrderPage.getOrderStatus()).containsIgnoringCase("PENDING");

        adminOrderPage.clickPaymentSuccess();

        assertThat(adminOrderPage.getOrderStatus()).containsIgnoringCase("CONFIRMED");
        assertThat(fixture.getOrderStatus(orderId)).isEqualTo("CONFIRMED");
    }

    @Test
    void paymentSuccess_createsShipments() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/orders/" + orderId);
        adminOrderPage.clickPaymentSuccess();

        await().atMost(10, TimeUnit.SECONDS).until(() -> fixture.countShipments(orderId) > 0);

        navigateTo("/admin/orders/" + orderId);
        assertThat(adminOrderPage.getShipmentCount()).isGreaterThan(0);
    }

    @Test
    void customerSeesShipmentAfterPaymentConfirmation() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders/" + orderId);
        adminOrderPage.clickPaymentSuccess();

        await().atMost(10, TimeUnit.SECONDS).until(() -> fixture.countShipments(orderId) > 0);

        loginAs(USER_EMAIL, USER_PASSWORD);
        navigateTo("/orders/" + orderId);

        assertThat(orderDetailPage.getOrderStatus()).containsIgnoringCase("CONFIRMED");
        assertThat(orderDetailPage.hasShipmentSection()).isTrue();
        assertThat(orderDetailPage.getShipmentTrackingNumbers()).isNotEmpty();
    }

    private UUID createOrder(UUID userId, UUID productId, UUID warehouseId) {
        UUID id = UUID.randomUUID();
        String orderNumber = "ORD-" + System.currentTimeMillis();
        Timestamp now = Timestamp.from(Instant.now());

        jdbcTemplate.update(
                "INSERT INTO orders (id, user_id, order_number, status, total_amount, total_currency, " +
                        "shipping_street, shipping_city, shipping_postal_code, shipping_country, created_at) " +
                        "VALUES (?, ?, ?, 'PENDING', ?, 'USD', ?, ?, ?, ?, ?)",
                id, userId, orderNumber, new BigDecimal("29.99"),
                "123 Ship St", "Shiptown", "90210", "US", now
        );

        UUID itemId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO order_items (id, order_id, product_id, product_name, quantity, " +
                        "unit_price_amount, unit_price_currency, warehouse_id) " +
                        "VALUES (?, ?, ?, 'Test Book', 1, ?, 'USD', ?)",
                itemId, id, productId, new BigDecimal("29.99"), warehouseId
        );

        return id;
    }
}
