package com.simpleshop.inventory.application.query;

import java.util.List;
import java.util.UUID;

public record StockAllocationResult(
    List<Allocation> allocations
) {
    public record Allocation(
        UUID productId,
        UUID warehouseId,
        int quantity
    ) {}
    
    public UUID getWarehouseIdForProduct(UUID productId) {
        return allocations.stream()
            .filter(a -> a.productId().equals(productId))
            .map(Allocation::warehouseId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No allocation found for product: " + productId));
    }
}
