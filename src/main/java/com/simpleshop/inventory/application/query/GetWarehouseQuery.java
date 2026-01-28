package com.simpleshop.inventory.application.query;

import java.util.UUID;

public record GetWarehouseQuery(UUID warehouseId) {
    public GetWarehouseQuery {
        if (warehouseId == null) throw new IllegalArgumentException("Warehouse ID is required");
    }
}
