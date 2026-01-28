package com.simpleshop.catalog.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
    String name,
    String description,
    String sku,
    BigDecimal price,
    String currency,
    UUID categoryId,
    String imageUrl
) {
    public CreateProductCommand {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU is required");
        if (price == null) throw new IllegalArgumentException("Price is required");
        if (currency == null) currency = "USD";
    }
}
