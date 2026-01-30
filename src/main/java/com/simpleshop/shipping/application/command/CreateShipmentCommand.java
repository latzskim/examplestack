package com.simpleshop.shipping.application.command;

import com.simpleshop.shared.domain.model.vo.Address;
import java.time.LocalDate;
import java.util.UUID;

public record CreateShipmentCommand(
    UUID orderId,
    UUID warehouseId,
    Address destinationAddress,
    LocalDate estimatedDelivery
) {}
