package com.simpleshop.order.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderPlaced extends DomainEvent {
    private final UUID orderId;
    private final String orderNumber;
    private final UUID userId;
    private final BigDecimal totalAmount;
    private final String currency;
    private final int itemCount;
    
    public OrderPlaced(UUID orderId, String orderNumber, UUID userId, 
                       BigDecimal totalAmount, String currency, int itemCount) {
        super();
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.itemCount = itemCount;
    }
    
    public UUID getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getUserId() { return userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public int getItemCount() { return itemCount; }
}
