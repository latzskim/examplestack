package com.simpleshop.order.application.command;

import java.util.UUID;

public record ShipOrderCommand(UUID orderId) {
    public ShipOrderCommand {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
    }
}
