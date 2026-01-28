package com.simpleshop.inventory.application.query;

import java.util.UUID;

public record ProductAvailabilityView(
    UUID productId,
    int totalAvailable,
    int totalReserved
) {}
