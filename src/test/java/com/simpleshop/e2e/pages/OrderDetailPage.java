package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

import java.util.List;
import java.util.UUID;

public class OrderDetailPage {

    private final Page page;

    public OrderDetailPage(Page page) {
        this.page = page;
    }

    public void navigate(UUID orderId) {
        page.navigate("/orders/" + orderId);
    }

    public String getOrderNumber() {
        return page.locator(".breadcrumb-item.active").textContent().trim();
    }

    public String getOrderStatus() {
        return page.locator(".badge.fs-6").first().textContent().trim();
    }

    public String getShippingAddress() {
        return page.locator("address").textContent().trim();
    }

    public List<String> getItemNames() {
        return page.locator("table.table tbody tr td:first-child").allTextContents()
                .stream().map(String::trim).toList();
    }

    public void cancelOrder() {
        page.onDialog(dialog -> dialog.accept());
        page.locator("button:has-text('Cancel Order')").click();
    }

    public boolean hasShipmentSection() {
        return page.locator(".card-header:has-text('Shipment Tracking')").isVisible();
    }

    public List<String> getShipmentTrackingNumbers() {
        return page.locator(".fw-bold.text-primary").allTextContents()
                .stream()
                .filter(t -> t.startsWith("SHIP-"))
                .map(String::trim)
                .toList();
    }
}
