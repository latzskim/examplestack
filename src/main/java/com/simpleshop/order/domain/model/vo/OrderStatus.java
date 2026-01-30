package com.simpleshop.order.domain.model.vo;

public enum OrderStatus {
    PENDING,      // Order placed, stock reserved, awaiting payment
    CONFIRMED,    // Payment received, stock confirmed (deducted from inventory)
    PROCESSING,   // Order being prepared for shipment
    SHIPPED,      // Order shipped to customer
    DELIVERED,    // Order delivered to customer
    CANCELLED;    // Order cancelled (only allowed from PENDING)
    
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PROCESSING || newStatus == SHIPPED;
            case PROCESSING -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
    
    public boolean isCancellable() {
        return this == PENDING;
    }
    
    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
