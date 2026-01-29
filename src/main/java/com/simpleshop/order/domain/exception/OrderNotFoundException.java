package com.simpleshop.order.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
    }
    
    public OrderNotFoundException(String orderNumber) {
        super("Order not found with number: " + orderNumber);
    }
}
