package com.simpleshop.order.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;

public class EmptyOrderException extends DomainException {
    public EmptyOrderException() {
        super("Order must contain at least one item");
    }
}
