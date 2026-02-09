package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

public class RegisterPage {

    private final Page page;

    public RegisterPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("/register");
    }

    public void register(String firstName, String lastName, String email, String password) {
        page.locator("#firstName").fill(firstName);
        page.locator("#lastName").fill(lastName);
        page.locator("#email").fill(email);
        page.locator("#password").fill(password);
        page.locator("button:has-text('Create Account')").click();
    }

    public String getEmailError() {
        return page.locator("#email ~ .invalid-feedback").textContent().trim();
    }

    public String getPasswordError() {
        return page.locator("#password ~ .invalid-feedback").textContent().trim();
    }

    public String getGeneralError() {
        return page.locator(".alert-danger").textContent().trim();
    }
}
