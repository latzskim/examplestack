package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.LoginPage;
import com.simpleshop.e2e.pages.RegisterPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Order(3)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegistrationLoginE2E extends E2EBaseTest {

    @Autowired
    TestDataFixture fixture;

    private static final String TEST_EMAIL = "new-user@example.com";
    private static final String TEST_PASSWORD = "SecurePass123";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();
    }

    @Test
    @Order(1)
    void shouldShowValidationErrorForEmptyEmail() {
        navigateTo("/register");
        page.locator("#firstName").fill(TEST_FIRST_NAME);
        page.locator("#lastName").fill(TEST_LAST_NAME);
        page.locator("#password").fill(TEST_PASSWORD);
        page.locator("button:has-text('Create Account')").click();

        assertThat(page.url()).contains("/register");
        boolean emailRequired = (boolean) page.locator("#email").evaluate("el => el.validity.valueMissing");
        assertThat(emailRequired).isTrue();
    }

    @Test
    @Order(2)
    void shouldShowValidationErrorForShortPassword() {
        navigateTo("/register");
        var registerPage = new RegisterPage(page);
        registerPage.register(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_EMAIL, "short");

        assertThat(registerPage.getPasswordError()).isNotEmpty();
    }

    @Test
    @Order(3)
    void shouldRegisterSuccessfully() {
        navigateTo("/register");
        var registerPage = new RegisterPage(page);
        registerPage.register(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_EMAIL, TEST_PASSWORD);

        var loginPage = new LoginPage(page);
        assertThat(loginPage.isOnLoginPage()).isTrue();
        assertThat(loginPage.getSuccessMessage()).isNotEmpty();
    }

    @Test
    @Order(4)
    void shouldRejectDuplicateEmail() {
        fixture.createUser(TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME, "USER");

        navigateTo("/register");
        var registerPage = new RegisterPage(page);
        registerPage.register(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_EMAIL, TEST_PASSWORD);

        assertThat(registerPage.getEmailError()).containsIgnoringCase("email already registered");
    }

    @Test
    @Order(5)
    void shouldLoginSuccessfully() {
        fixture.createUser(TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME, "USER");

        navigateTo("/login");
        var loginPage = new LoginPage(page);
        loginPage.login(TEST_EMAIL, TEST_PASSWORD);

        assertThat(page.url()).doesNotContain("/login");
    }
}
