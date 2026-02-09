package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.CartPage;
import com.simpleshop.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Order(4)
class CartMergeE2E extends E2EBaseTest {

    @Autowired
    TestDataFixture fixture;

    private static final String USER_EMAIL = "merge-user@example.com";
    private static final String USER_PASSWORD = "MergePass123";

    private UUID productAId;
    private UUID productBId;
    private UUID userId;

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();

        UUID categoryId = fixture.createCategory("Electronics");
        productAId = fixture.createProduct("Product A", "PROD-A", new BigDecimal("25.00"), "USD", categoryId, true);
        productBId = fixture.createProduct("Product B", "PROD-B", new BigDecimal("15.00"), "USD", categoryId, true);
        UUID warehouseId = fixture.createWarehouse("Main WH", "123 St", "NY", "10001", "US");
        fixture.addStock(productAId, warehouseId, 50);
        fixture.addStock(productBId, warehouseId, 50);

        userId = fixture.createUser(USER_EMAIL, USER_PASSWORD, "Merge", "User", "USER");
        UUID userCartId = fixture.createCartForUser(userId);
        fixture.addCartItem(userCartId, productAId, 1, new BigDecimal("25.00"), "USD");
        fixture.addCartItem(userCartId, productBId, 1, new BigDecimal("15.00"), "USD");
    }

    @Test
    void shouldMergeAnonymousCartWithUserCartOnLogin() {
        navigateTo("/products/" + productAId);
        var detail = new ProductDetailPage(page);
        detail.addToCart(2);

        navigateTo("/cart");
        var cart = new CartPage(page);
        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(cart.getItemQuantity("Product A")).isEqualTo("2");

        loginAs(USER_EMAIL, USER_PASSWORD);

        navigateTo("/cart");
        cart = new CartPage(page);

        assertThat(cart.getItemCount()).isEqualTo(2);
        assertThat(cart.getItemNames()).contains("Product A", "Product B");
        assertThat(cart.getItemQuantity("Product A")).isEqualTo("3");
        assertThat(cart.getItemQuantity("Product B")).isEqualTo("1");
    }

    @Test
    void shouldKeepUserCartIntactWhenNoAnonymousCartExists() {
        loginAs(USER_EMAIL, USER_PASSWORD);

        navigateTo("/cart");
        var cart = new CartPage(page);

        assertThat(cart.getItemCount()).isEqualTo(2);
        assertThat(cart.getItemNames()).contains("Product A", "Product B");
        assertThat(cart.getItemQuantity("Product A")).isEqualTo("1");
        assertThat(cart.getItemQuantity("Product B")).isEqualTo("1");
    }
}
