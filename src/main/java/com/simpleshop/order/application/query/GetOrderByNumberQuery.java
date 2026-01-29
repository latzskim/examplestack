package com.simpleshop.order.application.query;

public record GetOrderByNumberQuery(String orderNumber) {
    public GetOrderByNumberQuery {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("Order number is required");
        }
    }
}
