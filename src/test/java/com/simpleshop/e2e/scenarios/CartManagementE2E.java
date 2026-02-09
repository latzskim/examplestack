package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.CartPage;
import com.simpleshop.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Order(2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartManagementE2E extends E2EBaseTest {

    @Autowired
    TestDataFixture fixture;

    private UUID productId;

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();
        UUID categoryId = fixture.createCategory("Electronics");
        productId = fixture.createProduct("Wireless Mouse", "ELEC-100", new BigDecimal("49.99"), "USD", categoryId, true);
        UUID warehouseId = fixture.createWarehouse("Main WH", "123 St", "NY", "10001", "US");
        fixture.addStock(productId, warehouseId, 20);
    }

    @Test
    @Order(1)
    void shouldAddProductToCart() {
        navigateTo("/products/" + productId);
        var detail = new ProductDetailPage(page);
        detail.addToCart(2);

        navigateTo("/cart");
        var cart = new CartPage(page);

        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(cart.getItemNames()).contains("Wireless Mouse");
        assertThat(cart.getItemQuantity("Wireless Mouse")).isEqualTo("2");
    }

    @Test
    @Order(2)
    void shouldUpdateCartItemQuantity() {
        navigateTo("/products/" + productId);
        var detail = new ProductDetailPage(page);
        detail.addToCart(2);

        navigateTo("/cart");
        var cart = new CartPage(page);
        cart.updateQuantity("Wireless Mouse", 3);

        assertThat(cart.getItemQuantity("Wireless Mouse")).isEqualTo("3");
    }

    @Test
    @Order(3)
    void shouldCalculateCorrectTotal() {
        navigateTo("/products/" + productId);
        var detail = new ProductDetailPage(page);
        detail.addToCart(2);

        navigateTo("/cart");
        var cart = new CartPage(page);

        assertThat(cart.getTotal()).matches(".*99[.,]98.*");
    }

    @Test
    @Order(4)
    void shouldRemoveItemFromCart() {
        navigateTo("/products/" + productId);
        var detail = new ProductDetailPage(page);
        detail.addToCart(1);

        navigateTo("/cart");
        var cart = new CartPage(page);
        cart.removeItem("Wireless Mouse");

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    @Order(5)
    void shouldClearEntireCart() {
        navigateTo("/products/" + productId);
        var detail = new ProductDetailPage(page);
        detail.addToCart(2);

        navigateTo("/cart");
        var cart = new CartPage(page);
        cart.clearCart();

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    @Order(6)
    void shouldShowEmptyCartState() {
        navigateTo("/cart");
        var cart = new CartPage(page);

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    @Order(7)
    void shouldPersistCartAcrossPageNavigations() {
        navigateTo("/products/" + productId);
        var detail = new ProductDetailPage(page);
        detail.addToCart(2);

        navigateTo("/products");
        navigateTo("/cart");
        var cart = new CartPage(page);

        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(cart.getItemNames()).contains("Wireless Mouse");
    }
}
