package com.simpleshop.catalog.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class ProductNotFoundException extends DomainException {
    
    public ProductNotFoundException(UUID productId) {
        super("Product not found: " + productId);
    }
}
