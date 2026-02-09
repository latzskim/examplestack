package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.CartPage;
import com.simpleshop.e2e.pages.CheckoutPage;
import com.simpleshop.e2e.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("P0-05: Checkout guard and redirect behavior")
class CheckoutGuardE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private UUID categoryId;
    private UUID productId;
    private UUID warehouseId;

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();
        categoryId = fixture.createCategory("Electronics");
        productId = fixture.createProduct("Laptop", "LAP-001", new BigDecimal("999.99"), "USD", categoryId, true);
        warehouseId = fixture.createWarehouse("Main", "1 Main St", "NYC", "10001", "US");
        fixture.addStock(productId, warehouseId, 50);
    }

    @Test
    @DisplayName("Anonymous user accessing /checkout is redirected to /login")
    void anonymousUserRedirectedToLogin() {
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        assertThat(checkoutPage.isRedirectedToLogin()).isTrue();
    }

    @Test
    @DisplayName("After login, checkout form is accessible when user has cart items")
    void authenticatedUserWithCartCanAccessCheckout() {
        UUID userId = fixture.createUser("checkout-user@test.com", "password123", "Test", "User", "USER");
        UUID cartId = fixture.createCartForUser(userId);
        fixture.addCartItem(cartId, productId, 2, new BigDecimal("999.99"), "USD");

        loginAs("checkout-user@test.com", "password123");
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        assertThat(checkoutPage.isRedirectedToLogin()).isFalse();
        assertThat(checkoutPage.isRedirectedToCart()).isFalse();
        assertThat(page.url()).contains("/checkout");
    }

    @Test
    @DisplayName("Authenticated user with empty cart accessing /checkout is redirected to /cart")
    void authenticatedUserWithEmptyCartRedirectedToCart() {
        fixture.createUser("empty-cart@test.com", "password123", "Empty", "Cart", "USER");

        loginAs("empty-cart@test.com", "password123");
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        assertThat(checkoutPage.isRedirectedToCart()).isTrue();
    }
}
