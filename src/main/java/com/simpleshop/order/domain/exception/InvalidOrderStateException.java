package com.simpleshop.order.domain.exception;

import com.simpleshop.order.domain.model.vo.OrderStatus;
import com.simpleshop.shared.domain.exception.DomainException;

public class InvalidOrderStateException extends DomainException {
    public InvalidOrderStateException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Cannot transition order from " + currentStatus + " to " + targetStatus);
    }
}
