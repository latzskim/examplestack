package com.simpleshop.shipping.domain.model.vo;

public enum ShipmentStatus {
    CREATED,          // Shipment record created
    PICKED,           // Items picked from warehouse
    PACKED,           // Items packed and ready
    SHIPPED,          // Shipment handed to carrier
    IN_TRANSIT,       // Shipment in transit
    OUT_FOR_DELIVERY, // Out for delivery
    DELIVERED,        // Successfully delivered
    FAILED;           // Delivery failed
    
    public boolean canTransitionTo(ShipmentStatus newStatus) {
        return switch (this) {
            case CREATED -> newStatus == PICKED || newStatus == FAILED;
            case PICKED -> newStatus == PACKED || newStatus == FAILED;
            case PACKED -> newStatus == SHIPPED || newStatus == FAILED;
            case SHIPPED -> newStatus == IN_TRANSIT || newStatus == FAILED;
            case IN_TRANSIT -> newStatus == OUT_FOR_DELIVERY || newStatus == FAILED;
            case OUT_FOR_DELIVERY -> newStatus == DELIVERED || newStatus == FAILED;
            case DELIVERED, FAILED -> false;
        };
    }
    
    public boolean isFinal() {
        return this == DELIVERED || this == FAILED;
    }
    
    public String getDisplayName() {
        return switch (this) {
            case CREATED -> "Created";
            case PICKED -> "Picked";
            case PACKED -> "Packed";
            case SHIPPED -> "Shipped";
            case IN_TRANSIT -> "In Transit";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case DELIVERED -> "Delivered";
            case FAILED -> "Failed";
        };
    }
}
