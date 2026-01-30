package com.simpleshop.notification.domain.model.vo;

public enum NotificationType {
    ORDER_CONFIRMATION("Order Confirmation"),
    SHIPMENT_UPDATE("Shipment Update"),
    SHIPMENT_CREATED("Shipment Created"),
    INVOICE("Invoice"),
    USER_WELCOME("Welcome");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
