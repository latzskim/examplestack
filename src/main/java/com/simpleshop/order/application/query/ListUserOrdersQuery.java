package com.simpleshop.order.application.query;

import java.util.UUID;

public record ListUserOrdersQuery(
    UUID userId,
    int page,
    int size
) {
    public ListUserOrdersQuery {
        if (userId == null) throw new IllegalArgumentException("User ID is required");
        if (page < 0) throw new IllegalArgumentException("Page must be non-negative");
        if (size <= 0 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");
    }
    
    public ListUserOrdersQuery(UUID userId) {
        this(userId, 0, 20);
    }
}
