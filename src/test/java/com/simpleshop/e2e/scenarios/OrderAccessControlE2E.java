package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.OrderDetailPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("P0-08: User order access control")
class OrderAccessControlE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private UUID userAOrderId;
    private String orderNumber;

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();

        UUID categoryId = fixture.createCategory("Gadgets");
        UUID productId = fixture.createProduct("Widget", "WDG-001", new BigDecimal("19.99"), "USD", categoryId, true);
        UUID warehouseId = fixture.createWarehouse("Depot", "1 Depot Ln", "Denver", "80201", "US");
        fixture.addStock(productId, warehouseId, 50);

        UUID userAId = fixture.createUser("usera@test.com", "password123", "Alice", "A", "USER");
        fixture.createUser("userb@test.com", "password123", "Bob", "B", "USER");

        userAOrderId = UUID.randomUUID();
        orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Timestamp now = Timestamp.from(Instant.now());

        jdbcTemplate.update(
                "INSERT INTO orders (id, user_id, order_number, status, total_amount, total_currency, " +
                        "shipping_street, shipping_city, shipping_postal_code, shipping_country, created_at) " +
                        "VALUES (?, ?, ?, 'PENDING', ?, 'USD', ?, ?, ?, ?, ?)",
                userAOrderId, userAId, orderNumber, new BigDecimal("19.99"),
                "99 Main St", "Denver", "80201", "US", now
        );

        UUID orderItemId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO order_items (id, order_id, product_id, product_name, quantity, " +
                        "unit_price_amount, unit_price_currency, warehouse_id) " +
                        "VALUES (?, ?, ?, 'Widget', 1, 19.99, 'USD', ?)",
                orderItemId, userAOrderId, productId, warehouseId
        );
    }

    @Test
    @DisplayName("User B cannot view User A's order")
    void userBCannotViewUserAOrder() {
        loginAs("userb@test.com", "password123");

        navigateTo("/orders/" + userAOrderId);

        assertThat(page.content()).containsIgnoringCase("error");
    }

    @Test
    @DisplayName("User B cannot cancel User A's order")
    void userBCannotCancelUserAOrder() {
        loginAs("userb@test.com", "password123");

        page.navigate(baseUrl() + "/orders/" + userAOrderId + "/cancel",
                new com.microsoft.playwright.Page.NavigateOptions()
        );

        String status = fixture.getOrderStatus(userAOrderId);
        assertThat(status).isEqualTo("PENDING");
    }
}
