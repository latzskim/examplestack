package com.simpleshop.inventory.application.query;

import java.util.UUID;

public record CheckStockAvailabilityQuery(
    UUID productId,
    UUID warehouseId
) {
    public CheckStockAvailabilityQuery {
        if (productId == null) throw new IllegalArgumentException("Product ID is required");
    }
}
