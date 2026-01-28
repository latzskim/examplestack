package com.simpleshop.catalog.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;

public class DuplicateSkuException extends DomainException {
    
    public DuplicateSkuException(String sku) {
        super("Product with SKU already exists: " + sku);
    }
}
