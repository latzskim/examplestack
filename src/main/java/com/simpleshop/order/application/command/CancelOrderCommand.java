package com.simpleshop.order.application.command;

import java.util.UUID;

public record CancelOrderCommand(
    UUID orderId,
    String reason
) {
    public CancelOrderCommand {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
    }
}
