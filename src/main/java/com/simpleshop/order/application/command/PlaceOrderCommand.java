package com.simpleshop.order.application.command;

import java.util.List;
import java.util.UUID;

public record PlaceOrderCommand(
    UUID userId,
    String street,
    String city,
    String postalCode,
    String country,
    List<OrderItemData> items
) {
    public record OrderItemData(
        UUID productId,
        int quantity,
        UUID warehouseId
    ) {
        public OrderItemData {
            if (productId == null) throw new IllegalArgumentException("Product ID is required");
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    public PlaceOrderCommand {
        if (userId == null) throw new IllegalArgumentException("User ID is required");
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street is required");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City is required");
        if (postalCode == null || postalCode.isBlank()) throw new IllegalArgumentException("Postal code is required");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country is required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("At least one item is required");
    }
}
