package com.simpleshop.inventory.application.command;

import java.util.UUID;

public record ConfirmStockReservationCommand(
    UUID productId,
    UUID warehouseId,
    int quantity
) {
    public ConfirmStockReservationCommand {
        if (productId == null) throw new IllegalArgumentException("Product ID is required");
        if (warehouseId == null) throw new IllegalArgumentException("Warehouse ID is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
    }
}
