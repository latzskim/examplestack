package com.simpleshop.catalog.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class CategoryNotFoundException extends DomainException {
    
    public CategoryNotFoundException(UUID categoryId) {
        super("Category not found: " + categoryId);
    }
}
