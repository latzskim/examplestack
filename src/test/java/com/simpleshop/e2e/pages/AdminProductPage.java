package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

import java.math.BigDecimal;
import java.util.UUID;

public class AdminProductPage {

    private final Page page;

    public AdminProductPage(Page page) {
        this.page = page;
    }

    public void navigateToList() {
        page.navigate("/admin/products");
    }

    public void navigateToNewProduct() {
        page.navigate("/admin/products/new");
    }

    public void createProduct(String name, String sku, BigDecimal price, UUID categoryId) {
        page.locator("#name").fill(name);
        page.locator("#sku").fill(sku);
        page.locator("#price").fill(price.toPlainString());
        if (categoryId != null) {
            page.locator("#categoryId").selectOption(categoryId.toString());
        }
        page.locator("button:has-text('Create Product')").click();
    }

    public void deactivateProduct(UUID productId) {
        page.locator("form[action*='" + productId + "/deactivate'] button").click();
    }

    public void activateProduct(UUID productId) {
        page.locator("form[action*='" + productId + "/activate'] button").click();
    }

    public boolean isProductInList(String productName) {
        return page.locator("table.table tbody tr .fw-medium", new Page.LocatorOptions().setHasText(productName)).isVisible();
    }
}
