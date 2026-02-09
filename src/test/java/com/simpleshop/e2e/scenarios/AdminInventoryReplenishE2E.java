package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.AdminInventoryPage;
import com.simpleshop.e2e.pages.CartPage;
import com.simpleshop.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminInventoryReplenishE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private AdminInventoryPage adminInventoryPage;
    private ProductDetailPage productDetailPage;
    private CartPage cartPage;

    private static final String ADMIN_EMAIL = "admin-inv@test.com";
    private static final String ADMIN_PASSWORD = "Password123!";
    private static final String USER_EMAIL = "user-inv@test.com";
    private static final String USER_PASSWORD = "Password123!";

    private UUID productId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        fixture.cleanAll();
        fixture.createUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin", "Inv", "ADMIN");
        fixture.createUser(USER_EMAIL, USER_PASSWORD, "Regular", "User", "USER");

        UUID categoryId = fixture.createCategory("Gadgets");
        productId = fixture.createProduct("Empty Stock Item", "SKU-EMPTY-001",
                new BigDecimal("199.99"), "USD", categoryId, true);
        warehouseId = fixture.createWarehouse("Main Warehouse", "123 Main St", "Springfield", "62701", "US");
        fixture.addStock(productId, warehouseId, 0);

        adminInventoryPage = new AdminInventoryPage(page);
        productDetailPage = new ProductDetailPage(page);
        cartPage = new CartPage(page);
    }

    @Test
    void adminReplenishesStock_productBecomesAvailable() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/inventory/products/" + productId);
        adminInventoryPage.replenishStock(warehouseId, 50);

        assertThat(adminInventoryPage.getSuccessMessage()).contains("Stock");
        assertThat(fixture.getAvailableStock(productId)).isEqualTo(50);
    }

    @Test
    void userSeesAvailableStockAfterReplenish() {
        fixture.addStock(productId, warehouseId, 25);

        loginAs(USER_EMAIL, USER_PASSWORD);

        navigateTo("/products/" + productId);
        String stockStatus = productDetailPage.getStockStatus();
        assertThat(stockStatus).containsIgnoringCase("in stock");
    }

    @Test
    void userAddsReplenishedProductToCart() {
        fixture.addStock(productId, warehouseId, 10);

        loginAs(USER_EMAIL, USER_PASSWORD);

        navigateTo("/products/" + productId);
        productDetailPage.addToCart(1);

        navigateTo("/cart");
        assertThat(cartPage.getItemCount()).isEqualTo(1);
        assertThat(cartPage.getItemNames()).contains("Empty Stock Item");
    }
}
