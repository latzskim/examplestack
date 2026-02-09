package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

import java.util.UUID;

public class ProductDetailPage {

    private final Page page;

    public ProductDetailPage(Page page) {
        this.page = page;
    }

    public void navigate(UUID productId) {
        page.navigate("/products/" + productId);
    }

    public String getProductName() {
        return page.locator("h1").textContent().trim();
    }

    public String getProductPrice() {
        return page.locator(".fs-2.fw-bold.text-primary").textContent().trim();
    }

    public String getProductSku() {
        return page.locator("small:has-text('SKU:') span").textContent().trim();
    }

    public String getStockStatus() {
        return page.locator(".mb-4 .d-inline-flex.align-items-center.fw-medium").textContent().trim();
    }

    public void addToCart(int quantity) {
        page.locator("#quantity").fill(String.valueOf(quantity));
        page.locator("button:has-text('Add to Cart')").click();
    }

    public boolean isRedirectedToProducts() {
        return page.url().endsWith("/products");
    }
}
