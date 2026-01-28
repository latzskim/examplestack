package com.simpleshop.inventory.application.query;

import java.util.UUID;

public record StockView(
    UUID id,
    UUID productId,
    UUID warehouseId,
    int quantity,
    int reservedQuantity,
    int availableQuantity
) {}
