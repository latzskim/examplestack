package com.simpleshop.cart.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class CartItemNotFoundException extends DomainException {
    public CartItemNotFoundException(UUID productId) {
        super("Cart item not found for product: " + productId);
    }
    
    public CartItemNotFoundException(String message) {
        super(message);
    }
}
