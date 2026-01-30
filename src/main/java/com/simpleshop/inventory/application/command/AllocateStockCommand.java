package com.simpleshop.inventory.application.command;

import java.util.List;
import java.util.UUID;

public record AllocateStockCommand(
    List<AllocationRequest> items
) {
    public AllocateStockCommand {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }
    }
    
    public record AllocationRequest(
        UUID productId,
        int quantity
    ) {
        public AllocationRequest {
            if (productId == null) throw new IllegalArgumentException("Product ID is required");
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
