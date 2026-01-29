package com.simpleshop.cart.application.command;

import java.util.UUID;

public record RemoveItemFromCartCommand(
    String sessionId,
    UUID userId,
    UUID productId
) {
    public RemoveItemFromCartCommand {
        if (sessionId == null && userId == null) {
            throw new IllegalArgumentException("Either sessionId or userId is required");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
    }
}
