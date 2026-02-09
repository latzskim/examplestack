package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

public class OrderConfirmationPage {

    private final Page page;

    public OrderConfirmationPage(Page page) {
        this.page = page;
    }

    public String getOrderNumber() {
        return page.locator(".fs-5.fw-bold.text-primary").first().textContent().trim();
    }

    public String getOrderStatus() {
        return page.locator(".badge").first().textContent().trim();
    }

    public String getTotalAmount() {
        return page.locator("tfoot .text-end.fw-bold.fs-5.text-primary").textContent().trim();
    }
}
