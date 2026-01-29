package com.simpleshop.cart.application.query;

import java.util.UUID;

public record GetCartQuery(
    String sessionId,
    UUID userId
) {
    public GetCartQuery {
        if (sessionId == null && userId == null) {
            throw new IllegalArgumentException("Either sessionId or userId is required");
        }
    }
}
