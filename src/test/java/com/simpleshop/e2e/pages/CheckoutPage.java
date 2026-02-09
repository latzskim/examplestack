package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

public class CheckoutPage {

    private final Page page;

    public CheckoutPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("/checkout");
    }

    public void fillShippingAddress(String street, String city, String postalCode, String country) {
        page.locator("#street").fill(street);
        page.locator("#city").fill(city);
        page.locator("#postalCode").fill(postalCode);
        page.locator("#country").fill(country);
    }

    public void submitOrder() {
        page.locator("button:has-text('Place Order')").click();
    }

    public String getErrorMessage() {
        return page.locator(".alert-danger").textContent().trim();
    }

    public boolean isRedirectedToCart() {
        return page.url().endsWith("/cart");
    }

    public boolean isRedirectedToLogin() {
        return page.url().contains("/login");
    }
}
