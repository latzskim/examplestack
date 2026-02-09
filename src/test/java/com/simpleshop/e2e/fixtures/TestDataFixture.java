package com.simpleshop.e2e.fixtures;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
public class TestDataFixture {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public TestDataFixture(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public UUID createCategory(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO categories (id, name, description, parent_id, sort_order, created_at) VALUES (?, ?, ?, NULL, 0, ?)",
            id, name, "Description for " + name, now()
        );
        return id;
    }

    public UUID createProduct(String name, String sku, BigDecimal price, String currency, UUID categoryId, boolean active) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO products (id, name, description, sku, price_amount, price_currency, category_id, image_url, active, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?)",
            id, name, "Description for " + name, sku, price, currency, categoryId, active, now(), now()
        );
        return id;
    }

    public UUID createWarehouse(String name, String street, String city, String postalCode, String country) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO warehouses (id, name, warehouse_street, warehouse_city, warehouse_postal_code, warehouse_country, active, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, true, ?)",
            id, name, street, city, postalCode, country, now()
        );
        return id;
    }

    public UUID addStock(UUID productId, UUID warehouseId, int quantity) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO stocks (id, product_id, warehouse_id, quantity, reserved_quantity, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, 0, ?, ?)",
            id, productId, warehouseId, quantity, now(), now()
        );
        return id;
    }

    public UUID createUser(String email, String password, String firstName, String lastName, String role) {
        UUID id = UUID.randomUUID();
        String encodedPassword = passwordEncoder.encode(password);
        jdbcTemplate.update(
            "INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?)",
            id, email, encodedPassword, firstName, lastName, role, now()
        );
        return id;
    }

    public UUID createCartForUser(UUID userId) {
        UUID cartId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO carts (id, user_id, session_id, created_at, updated_at) VALUES (?, ?, NULL, ?, ?)",
            cartId, userId, now(), now()
        );
        return cartId;
    }

    public UUID createCartForSession(String sessionId) {
        UUID cartId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO carts (id, user_id, session_id, created_at, updated_at) VALUES (?, NULL, ?, ?, ?)",
            cartId, sessionId, now(), now()
        );
        return cartId;
    }

    public void addCartItem(UUID cartId, UUID productId, int quantity, BigDecimal price, String currency) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO cart_items (id, cart_id, product_id, quantity, price_amount, price_currency) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            id, cartId, productId, quantity, price, currency
        );
    }

    public int getAvailableStock(UUID productId) {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(quantity - reserved_quantity), 0) FROM stocks WHERE product_id = ?",
            Integer.class, productId
        );
        return result != null ? result : 0;
    }

    public int getReservedStock(UUID productId) {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(reserved_quantity), 0) FROM stocks WHERE product_id = ?",
            Integer.class, productId
        );
        return result != null ? result : 0;
    }

    public String getOrderStatus(UUID orderId) {
        return jdbcTemplate.queryForObject(
            "SELECT status FROM orders WHERE id = ?",
            String.class, orderId
        );
    }

    public int countShipments(UUID orderId) {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM shipments WHERE order_id = ?",
            Integer.class, orderId
        );
        return result != null ? result : 0;
    }

    public int countNotificationLogs() {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_logs",
            Integer.class
        );
        return result != null ? result : 0;
    }

    public int countNotificationLogsByRecipient(String email) {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_logs WHERE value = ?",
            Integer.class, email
        );
        return result != null ? result : 0;
    }

    private Timestamp now() {
        return Timestamp.from(Instant.now());
    }

    public void cleanAll() {
        jdbcTemplate.execute("DELETE FROM notification_logs");
        jdbcTemplate.execute("DELETE FROM shipment_status_history");
        jdbcTemplate.execute("DELETE FROM shipments");
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM cart_items");
        jdbcTemplate.execute("DELETE FROM carts");
        jdbcTemplate.execute("DELETE FROM stocks");
        jdbcTemplate.execute("DELETE FROM products");
        jdbcTemplate.execute("DELETE FROM categories");
        jdbcTemplate.execute("DELETE FROM users");
    }
}
