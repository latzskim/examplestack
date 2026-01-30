package com.simpleshop.notification.application.port.out;

import com.simpleshop.shipping.application.query.ShipmentView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentQueryPort {
    
    Optional<ShipmentView> getShipmentById(UUID shipmentId);
    
    List<ShipmentView> getShipmentsByOrderId(UUID orderId);
}
