package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

public class ProductListPage {

    private final Page page;

    public ProductListPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("/products");
    }

    public Locator getProductCards() {
        return page.locator(".product-card");
    }

    public List<String> getProductNames() {
        return page.locator(".product-card .card-title").allTextContents();
    }

    public void filterByCategory(String categoryName) {
        page.locator(".list-group-item-action", new Page.LocatorOptions().setHasText(categoryName)).click();
    }

    public void clickProductDetail(String productName) {
        page.locator(".product-card", new Page.LocatorOptions().setHasText(productName))
                .getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Locator.GetByRoleOptions().setName("View Details"))
                .click();
    }

    public boolean isProductVisible(String productName) {
        return page.locator(".product-card .card-title", new Page.LocatorOptions().setHasText(productName)).isVisible();
    }
}
