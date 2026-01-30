package com.simpleshop.shipping.application.query;

import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ShipmentTrackingView(
    UUID shipmentId,
    String trackingNumber,
    UUID orderId,
    ShipmentStatus currentStatus,
    String destinationAddress,
    List<ShipmentStatusHistoryView> statusHistory
) {
    public record ShipmentStatusHistoryView(
        ShipmentStatus status,
        Instant changedAt,
        String location,
        String notes
    ) {}
}
