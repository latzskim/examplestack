package com.simpleshop.cart.application.command;

import java.util.UUID;

public record UpdateItemQuantityCommand(
    String sessionId,
    UUID userId,
    UUID productId,
    int quantity
) {
    public UpdateItemQuantityCommand {
        if (sessionId == null && userId == null) {
            throw new IllegalArgumentException("Either sessionId or userId is required");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }
}
