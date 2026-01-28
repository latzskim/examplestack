package com.simpleshop.inventory.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class InsufficientStockException extends DomainException {
    
    public InsufficientStockException(UUID productId, int available, int requested) {
        super("Insufficient stock for product " + productId + ". Available: " + available + ", Requested: " + requested);
    }
}
