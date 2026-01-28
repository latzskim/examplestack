package com.simpleshop.inventory.application.query;

import java.util.UUID;

public record WarehouseView(
    UUID id,
    String name,
    String street,
    String city,
    String postalCode,
    String country,
    boolean active
) {}
