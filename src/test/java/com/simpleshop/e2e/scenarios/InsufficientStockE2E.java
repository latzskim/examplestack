package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.CartPage;
import com.simpleshop.e2e.pages.CheckoutPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("P0-07: Checkout failure when stock insufficient")
class InsufficientStockE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private UUID productId;

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();

        UUID categoryId = fixture.createCategory("Books");
        productId = fixture.createProduct("Rare Book", "RB-001", new BigDecimal("49.99"), "USD", categoryId, true);
        UUID warehouseId = fixture.createWarehouse("Small Depot", "5 Depot Rd", "Boston", "02101", "US");
        fixture.addStock(productId, warehouseId, 2);

        UUID userId = fixture.createUser("stock-test@test.com", "password123", "Low", "Stock", "USER");
        UUID cartId = fixture.createCartForUser(userId);
        fixture.addCartItem(cartId, productId, 5, new BigDecimal("49.99"), "USD");

        loginAs("stock-test@test.com", "password123");
    }

    @Test
    @DisplayName("Checkout fails with error when cart qty exceeds available stock")
    void checkoutFailsWithInsufficientStock() {
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        checkoutPage.fillShippingAddress("10 Short St", "Boston", "02101", "US");
        checkoutPage.submitOrder();

        String error = checkoutPage.getErrorMessage();
        assertThat(error).isNotBlank();
    }

    @Test
    @DisplayName("No order is created when stock is insufficient")
    void noOrderCreatedOnFailure() {
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        checkoutPage.fillShippingAddress("10 Short St", "Boston", "02101", "US");
        checkoutPage.submitOrder();

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'stock-test@test.com')",
                Integer.class
        );
        assertThat(orderCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Cart remains unchanged after failed checkout")
    void cartUnchangedAfterFailure() {
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        checkoutPage.fillShippingAddress("10 Short St", "Boston", "02101", "US");
        checkoutPage.submitOrder();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(page);
        assertThat(cartPage.isEmpty()).isFalse();
        assertThat(cartPage.getItemCount()).isEqualTo(1);
        assertThat(cartPage.getItemQuantity("Rare Book")).isEqualTo("5");
    }
}
