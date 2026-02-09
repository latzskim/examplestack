package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

import java.util.UUID;

public class AdminInventoryPage {

    private final Page page;

    public AdminInventoryPage(Page page) {
        this.page = page;
    }

    public void navigate(UUID productId) {
        page.navigate("/admin/inventory/products/" + productId);
    }

    public void replenishStock(UUID warehouseId, int quantity) {
        page.locator("#warehouseId").selectOption(warehouseId.toString());
        page.locator("#quantity").fill(String.valueOf(quantity));
        page.locator("button:has-text('Add Stock')").click();
    }

    public String getTotalAvailable() {
        return page.locator(".bg-success.text-white h3").textContent().trim();
    }

    public String getSuccessMessage() {
        return page.locator(".alert-success").textContent().trim();
    }
}
