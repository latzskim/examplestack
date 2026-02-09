package com.simpleshop.e2e.pages;

import com.microsoft.playwright.Page;

import java.util.List;

public class ShipmentTrackingPage {

    private final Page page;

    public ShipmentTrackingPage(Page page) {
        this.page = page;
    }

    public void navigate(String trackingNumber) {
        page.navigate("/shipments/track/" + trackingNumber);
    }

    public String getCurrentStatus() {
        return page.locator(".card-header .badge.fs-6").textContent().trim();
    }

    public List<String> getStatusHistoryEntries() {
        return page.locator(".timeline-item .timeline-content h6")
                .allTextContents()
                .stream()
                .map(String::trim)
                .toList();
    }

    public String getTrackingNumber() {
        return page.locator(".fs-5.fw-bold.text-primary").textContent().trim();
    }
}
