package com.simpleshop.cart.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class EmptyCartException extends DomainException {
    public EmptyCartException(UUID cartId) {
        super("Cart is empty: " + cartId);
    }
    
    public EmptyCartException(String message) {
        super(message);
    }
}
