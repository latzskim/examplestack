package com.simpleshop.catalog.application.query;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductListView(
    UUID id,
    String name,
    String sku,
    BigDecimal price,
    String currency,
    String imageUrl,
    boolean active
) {}
