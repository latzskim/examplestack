package com.simpleshop.order.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class OrderAlreadyCancelledException extends DomainException {
    public OrderAlreadyCancelledException(UUID orderId) {
        super("Order already cancelled: " + orderId);
    }
}
