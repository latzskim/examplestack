package com.simpleshop.shipping.application.port.in;

import com.simpleshop.shipping.application.query.ShipmentView;
import java.util.Optional;
import java.util.UUID;

public interface GetShipmentUseCase {
    Optional<ShipmentView> getShipment(UUID shipmentId);
    Optional<ShipmentView> getShipmentByTrackingNumber(String trackingNumber);
}
