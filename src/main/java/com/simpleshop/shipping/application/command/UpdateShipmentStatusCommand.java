package com.simpleshop.shipping.application.command;

import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import java.util.UUID;

public record UpdateShipmentStatusCommand(
    UUID shipmentId,
    ShipmentStatus newStatus,
    String location,
    String notes
) {}
