package com.simpleshop.catalog.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductCommand(
    UUID productId,
    String name,
    String description,
    BigDecimal price,
    String currency,
    UUID categoryId,
    String imageUrl
) {
    public UpdateProductCommand {
        if (productId == null) throw new IllegalArgumentException("Product ID is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (price == null) throw new IllegalArgumentException("Price is required");
        if (currency == null) currency = "USD";
    }
}
