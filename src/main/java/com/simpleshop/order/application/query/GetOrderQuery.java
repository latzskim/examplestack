package com.simpleshop.order.application.query;

import java.util.UUID;

public record GetOrderQuery(UUID orderId) {
    public GetOrderQuery {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
    }
}
