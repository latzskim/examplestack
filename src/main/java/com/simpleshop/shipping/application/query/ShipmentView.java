package com.simpleshop.shipping.application.query;

import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShipmentView(
    UUID id,
    String trackingNumber,
    UUID orderId,
    UUID warehouseId,
    String destinationStreet,
    String destinationCity,
    String destinationPostalCode,
    String destinationCountry,
    ShipmentStatus status,
    LocalDate estimatedDelivery,
    Instant createdAt
) {}
