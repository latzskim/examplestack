package com.simpleshop.order.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class OrderCancelled extends DomainEvent {
    private final UUID orderId;
    private final String orderNumber;
    private final UUID userId;
    private final String reason;
    
    public OrderCancelled(UUID orderId, String orderNumber, UUID userId, String reason) {
        super();
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.reason = reason;
    }
    
    public UUID getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getUserId() { return userId; }
    public String getReason() { return reason; }
}
