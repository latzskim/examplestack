package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

import java.util.List;

public class OrderListPage {

    private final Page page;

    public OrderListPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("/orders");
    }

    public int getOrderCount() {
        return page.locator("table.table tbody tr").count();
    }

    public List<String> getOrderNumbers() {
        return page.locator("table.table tbody tr td a.fw-bold").allTextContents();
    }

    public void clickOrder(String orderNumber) {
        page.locator("table.table tbody tr td a.fw-bold", new Page.LocatorOptions().setHasText(orderNumber)).click();
    }

    public boolean isEmpty() {
        return page.locator(".empty-state-title:has-text('No orders yet')").isVisible();
    }
}
