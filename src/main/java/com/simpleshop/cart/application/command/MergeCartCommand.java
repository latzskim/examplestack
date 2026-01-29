package com.simpleshop.cart.application.command;

import java.util.UUID;

public record MergeCartCommand(
    String sessionId,
    UUID userId
) {
    public MergeCartCommand {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID is required for merge");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required for merge");
        }
    }
}
