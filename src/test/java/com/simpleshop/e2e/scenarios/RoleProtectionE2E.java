package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class RoleProtectionE2E extends E2EBaseTest {

    @Autowired
    private TestDataFixture fixture;

    private static final String ADMIN_EMAIL = "admin-role@test.com";
    private static final String ADMIN_PASSWORD = "Password123!";
    private static final String USER_EMAIL = "user-role@test.com";
    private static final String USER_PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        fixture.cleanAll();
        fixture.createUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin", "Role", "ADMIN");
        fixture.createUser(USER_EMAIL, USER_PASSWORD, "Regular", "Role", "USER");
    }

    @Test
    void anonymousAccessToAdminProducts_redirectsToLogin() {
        navigateTo("/admin/products");
        assertThat(page.url()).contains("/login");
    }

    @Test
    void anonymousAccessToOrders_redirectsToLogin() {
        navigateTo("/orders");
        assertThat(page.url()).contains("/login");
    }

    @Test
    void regularUserAccessToAdminOrders_isForbidden() {
        loginAs(USER_EMAIL, USER_PASSWORD);
        navigateTo("/admin/orders");

        boolean isForbidden = page.url().contains("/error") ||
                page.url().contains("/access-denied") ||
                page.locator("body").textContent().toLowerCase().contains("forbidden") ||
                page.locator("body").textContent().toLowerCase().contains("403") ||
                page.locator("body").textContent().toLowerCase().contains("access denied");
        assertThat(isForbidden).isTrue();
    }

    @Test
    void adminCanAccessAdminOrders() {
        loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        navigateTo("/admin/orders");
        assertThat(page.url()).contains("/admin/orders");
    }

    @Test
    void publicRoutes_accessibleAnonymously() {
        navigateTo("/products");
        assertThat(page.url()).contains("/products");

        navigateTo("/cart");
        assertThat(page.url()).contains("/cart");

        navigateTo("/login");
        assertThat(page.url()).contains("/login");

        navigateTo("/register");
        assertThat(page.url()).contains("/register");
    }
}
