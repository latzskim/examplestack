package com.simpleshop.catalog.application.query;

import java.util.UUID;

public record ListProductsQuery(
    UUID categoryId,
    Boolean activeOnly,
    int page,
    int size
) {
    public ListProductsQuery {
        if (page < 0) throw new IllegalArgumentException("Page must be non-negative");
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
    }
    
    public static ListProductsQuery ofActive(int page, int size) {
        return new ListProductsQuery(null, true, page, size);
    }
    
    public static ListProductsQuery all(int page, int size) {
        return new ListProductsQuery(null, null, page, size);
    }
}
