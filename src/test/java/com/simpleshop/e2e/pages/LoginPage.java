package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("/login");
    }

    public void login(String email, String password) {
        page.locator("#username").fill(email);
        page.locator("#password").fill(password);
        page.locator("button:has-text('Sign In')").click();
    }

    public String getErrorMessage() {
        return page.locator(".alert-danger").textContent().trim();
    }

    public String getSuccessMessage() {
        return page.locator(".alert-success").textContent().trim();
    }

    public boolean isOnLoginPage() {
        return page.url().contains("/login");
    }
}
