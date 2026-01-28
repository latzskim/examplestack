package com.simpleshop.inventory.application.command;

public record CreateWarehouseCommand(
    String name,
    String street,
    String city,
    String postalCode,
    String country
) {
    public CreateWarehouseCommand {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Warehouse name is required");
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street is required");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City is required");
        if (postalCode == null || postalCode.isBlank()) throw new IllegalArgumentException("Postal code is required");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country is required");
    }
}
