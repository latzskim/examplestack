package com.simpleshop.order.application.command;

import java.util.UUID;

public record DeliverOrderCommand(UUID orderId) {
    public DeliverOrderCommand {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
    }
}
