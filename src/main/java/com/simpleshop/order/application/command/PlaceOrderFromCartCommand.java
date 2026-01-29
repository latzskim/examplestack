package com.simpleshop.order.application.command;

import java.util.UUID;

public record PlaceOrderFromCartCommand(
    UUID userId,
    String sessionId,
    String street,
    String city,
    String postalCode,
    String country,
    UUID warehouseId
) {
    public PlaceOrderFromCartCommand {
        if (userId == null && sessionId == null) throw new IllegalArgumentException("Either userId or sessionId is required");
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street is required");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City is required");
        if (postalCode == null || postalCode.isBlank()) throw new IllegalArgumentException("Postal code is required");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country is required");
    }
}
