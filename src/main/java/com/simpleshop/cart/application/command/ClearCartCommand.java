package com.simpleshop.cart.application.command;

import java.util.UUID;

public record ClearCartCommand(
    String sessionId,
    UUID userId
) {
    public ClearCartCommand {
        if (sessionId == null && userId == null) {
            throw new IllegalArgumentException("Either sessionId or userId is required");
        }
    }
}
