package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.UUID;

public class AdminOrderPage {

    private final Page page;

    public AdminOrderPage(Page page) {
        this.page = page;
    }

    public void navigate(UUID orderId) {
        page.navigate("/admin/orders/" + orderId);
    }

    public String getOrderStatus() {
        return page.locator(".badge.fs-5").textContent().trim();
    }

    public void clickPaymentSuccess() {
        page.locator("button:has-text('Payment Success')").click();
    }

    public void clickPaymentFailed() {
        page.locator("button:has-text('Payment Failed')").click();
    }

    public String getSuccessMessage() {
        return page.locator(".alert-success").textContent().trim();
    }

    public String getErrorMessage() {
        return page.locator(".alert-danger").textContent().trim();
    }

    public int getShipmentCount() {
        return page.locator(".card-header:has-text('Shipments') + .card-body > div.mb-4").count();
    }

    public List<String> getShipmentTrackingNumbers() {
        return page.locator(".card-header:has-text('Shipments') + .card-body .fw-bold.text-primary")
                .allTextContents()
                .stream()
                .map(String::trim)
                .toList();
    }

    public void updateShipmentStatus(String trackingNumber, String newStatus) {
        Locator shipmentBlock = page.locator(".card-body > div.mb-4",
                new Page.LocatorOptions().setHasText(trackingNumber));
        shipmentBlock.locator("select[name='newStatus']").selectOption(newStatus);
        shipmentBlock.locator("button:has-text('Update')").click();
    }
}
