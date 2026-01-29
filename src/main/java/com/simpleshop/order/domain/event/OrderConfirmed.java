package com.simpleshop.order.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class OrderConfirmed extends DomainEvent {
    private final UUID orderId;
    private final String orderNumber;
    private final UUID userId;
    
    public OrderConfirmed(UUID orderId, String orderNumber, UUID userId) {
        super();
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
    }
    
    public UUID getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getUserId() { return userId; }
}
