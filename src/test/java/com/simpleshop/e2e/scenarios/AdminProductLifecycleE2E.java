package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.AdminProductPage;
import com.simpleshop.e2e.pages.ProductDetailPage;
import com.simpleshop.e2e.pages.ProductListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminProductLifecycleE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private AdminProductPage adminProductPage;
    private ProductListPage productListPage;
    private ProductDetailPage productDetailPage;

    private static final String ADMIN_EMAIL = "admin-plc@test.com";
    private static final String ADMIN_PASSWORD = "Password123!";

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        fixture.cleanAll();
        fixture.createUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin", "User", "ADMIN");
        categoryId = fixture.createCategory("Electronics");
        adminProductPage = new AdminProductPage(page);
        productListPage = new ProductListPage(page);
        productDetailPage = new ProductDetailPage(page);
    }

    @Test
    void adminCreatesProduct_visibleInAdminListAndStorefront() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/products/new");
        adminProductPage.createProduct("Test Laptop", "SKU-LAPTOP-001", new BigDecimal("999.99"), categoryId);

        navigateTo("/admin/products");
        assertThat(adminProductPage.isProductInList("Test Laptop")).isTrue();

        navigateTo("/products");
        assertThat(productListPage.isProductVisible("Test Laptop")).isTrue();
    }

    @Test
    void adminDeactivatesProduct_disappearsFromStorefront() {
        UUID productId = fixture.createProduct("Deactivate Me", "SKU-DEACT-001",
                new BigDecimal("49.99"), "USD", categoryId, true);

        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/products");
        adminProductPage.deactivateProduct(productId);

        navigateTo("/products");
        assertThat(productListPage.isProductVisible("Deactivate Me")).isFalse();
    }

    @Test
    void directAccessToDeactivatedProduct_redirectsToProducts() {
        UUID productId = fixture.createProduct("Inactive Product", "SKU-INACT-001",
                new BigDecimal("29.99"), "USD", categoryId, false);

        navigateTo("/products/" + productId);
        assertThat(productDetailPage.isRedirectedToProducts()).isTrue();
    }

    @Test
    void adminReactivatesProduct_reappearsInStorefront() {
        UUID productId = fixture.createProduct("Reactivate Me", "SKU-REACT-001",
                new BigDecimal("79.99"), "USD", categoryId, false);

        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        navigateTo("/admin/products");
        adminProductPage.activateProduct(productId);

        navigateTo("/products");
        assertThat(productListPage.isProductVisible("Reactivate Me")).isTrue();
    }
}
