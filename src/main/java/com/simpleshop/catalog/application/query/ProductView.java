package com.simpleshop.catalog.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductView(
    UUID id,
    String name,
    String description,
    String sku,
    BigDecimal price,
    String currency,
    UUID categoryId,
    String categoryName,
    String imageUrl,
    boolean active,
    Instant createdAt
) {}
