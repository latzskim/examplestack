package com.simpleshop.inventory.application.query;

import java.util.UUID;

public record GetWarehouseStockQuery(
    UUID warehouseId,
    int page,
    int size
) {
    public GetWarehouseStockQuery {
        if (warehouseId == null) throw new IllegalArgumentException("Warehouse ID is required");
        if (page < 0) throw new IllegalArgumentException("Page must be non-negative");
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
    }
}
