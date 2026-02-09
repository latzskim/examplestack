package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

public class CartPage {

    private final Page page;

    public CartPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("/cart");
    }

    public int getItemCount() {
        return page.locator("table.table tbody tr").count();
    }

    public List<String> getItemNames() {
        return page.locator("table.table tbody tr td .fw-medium.text-decoration-none").allTextContents();
    }

    public String getItemQuantity(String productName) {
        Locator row = page.locator("table.table tbody tr", new Page.LocatorOptions().setHasText(productName));
        return row.locator("input[name='quantity']").inputValue();
    }

    public void updateQuantity(String productName, int newQuantity) {
        Locator row = page.locator("table.table tbody tr", new Page.LocatorOptions().setHasText(productName));
        row.locator("input[name='quantity']").fill(String.valueOf(newQuantity));
        row.locator("form[action*='/cart/update'] button[type='submit']").click();
    }

    public void removeItem(String productName) {
        page.onDialog(dialog -> dialog.accept());
        Locator row = page.locator("table.table tbody tr", new Page.LocatorOptions().setHasText(productName));
        row.locator("form[action*='/cart/remove'] button[type='submit']").click();
    }

    public void clearCart() {
        page.onDialog(dialog -> dialog.accept());
        page.locator("button:has-text('Clear Cart')").click();
    }

    public String getTotal() {
        return page.locator(".card-body .fs-4.fw-bold.text-primary").first().textContent().trim();
    }

    public boolean isEmpty() {
        return page.locator(".empty-state-title:has-text('Your cart is empty')").isVisible();
    }

    public void proceedToCheckout() {
        page.locator("a:has-text('Proceed to Checkout')").click();
    }
}
