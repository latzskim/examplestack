package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.CartPage;
import com.simpleshop.e2e.pages.CheckoutPage;
import com.simpleshop.e2e.pages.OrderConfirmationPage;
import com.simpleshop.e2e.pages.OrderDetailPage;
import com.simpleshop.e2e.pages.OrderListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("P0-06: Place order happy path")
class PlaceOrderE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private UUID productId;

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();

        UUID categoryId = fixture.createCategory("Electronics");
        productId = fixture.createProduct("Wireless Mouse", "WM-001", new BigDecimal("29.99"), "USD", categoryId, true);
        UUID warehouseId = fixture.createWarehouse("Central", "10 Warehouse Ave", "Chicago", "60601", "US");
        fixture.addStock(productId, warehouseId, 100);

        UUID userId = fixture.createUser("buyer@test.com", "password123", "Jane", "Doe", "USER");
        UUID cartId = fixture.createCartForUser(userId);
        fixture.addCartItem(cartId, productId, 3, new BigDecimal("29.99"), "USD");

        loginAs("buyer@test.com", "password123");
    }

    @Test
    @DisplayName("User completes checkout and order is created with status PENDING")
    void placeOrderHappyPath() {
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        checkoutPage.fillShippingAddress("123 Elm St", "Springfield", "62701", "US");
        checkoutPage.submitOrder();

        page.waitForURL(url -> url.contains("/checkout/confirmation/"));

        OrderConfirmationPage confirmationPage = new OrderConfirmationPage(page);
        String orderNumber = confirmationPage.getOrderNumber();
        assertThat(orderNumber).isNotBlank();

        String status = confirmationPage.getOrderStatus();
        assertThat(status).containsIgnoringCase("PENDING");
    }

    @Test
    @DisplayName("Cart is cleared after successful order placement")
    void cartClearedAfterOrder() {
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        checkoutPage.fillShippingAddress("123 Elm St", "Springfield", "62701", "US");
        checkoutPage.submitOrder();

        page.waitForURL(url -> url.contains("/checkout/confirmation/"));

        navigateTo("/cart");
        CartPage cartPage = new CartPage(page);
        assertThat(cartPage.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Stock is reserved after order placement")
    void stockReservedAfterOrder() {
        int reservedBefore = fixture.getReservedStock(productId);

        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        checkoutPage.fillShippingAddress("123 Elm St", "Springfield", "62701", "US");
        checkoutPage.submitOrder();

        page.waitForURL(url -> url.contains("/checkout/confirmation/"));

        int reservedAfter = fixture.getReservedStock(productId);
        assertThat(reservedAfter).isEqualTo(reservedBefore + 3);
    }

    @Test
    @DisplayName("Order appears in order list and detail shows correct info")
    void orderAppearsInListAndDetail() {
        navigateTo("/checkout");

        CheckoutPage checkoutPage = new CheckoutPage(page);
        checkoutPage.fillShippingAddress("456 Oak Ave", "Portland", "97201", "US");
        checkoutPage.submitOrder();

        page.waitForURL(url -> url.contains("/checkout/confirmation/"));

        OrderConfirmationPage confirmationPage = new OrderConfirmationPage(page);
        String orderNumber = confirmationPage.getOrderNumber();

        OrderListPage orderListPage = new OrderListPage(page);
        navigateTo("/orders");
        assertThat(orderListPage.getOrderCount()).isGreaterThanOrEqualTo(1);
        assertThat(orderListPage.getOrderNumbers()).contains(orderNumber);

        orderListPage.clickOrder(orderNumber);

        OrderDetailPage detailPage = new OrderDetailPage(page);
        assertThat(detailPage.getOrderNumber()).contains(orderNumber);
        assertThat(detailPage.getOrderStatus()).containsIgnoringCase("PENDING");
        assertThat(detailPage.getShippingAddress()).contains("Portland");
        assertThat(detailPage.getItemNames()).contains("Wireless Mouse");
    }
}
