package com.simpleshop.order.application.command;

import java.util.UUID;

public record ConfirmOrderCommand(UUID orderId) {
    public ConfirmOrderCommand {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
    }
}
