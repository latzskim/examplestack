package com.simpleshop.cart.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class CartNotFoundException extends DomainException {
    public CartNotFoundException(UUID cartId) {
        super("Cart not found with id: " + cartId);
    }
    
    public CartNotFoundException(String message) {
        super(message);
    }
}
