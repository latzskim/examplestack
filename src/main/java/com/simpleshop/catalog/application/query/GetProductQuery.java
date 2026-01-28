package com.simpleshop.catalog.application.query;

import java.util.UUID;

public record GetProductQuery(UUID productId) {
    public GetProductQuery {
        if (productId == null) throw new IllegalArgumentException("Product ID is required");
    }
}
