package com.simpleshop.shipping.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class ShipmentCreated extends DomainEvent {
    private final UUID shipmentId;
    private final String trackingNumber;
    private final UUID orderId;
    private final UUID warehouseId;
    
    public ShipmentCreated(UUID shipmentId, String trackingNumber, UUID orderId, UUID warehouseId) {
        super();
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.orderId = orderId;
        this.warehouseId = warehouseId;
    }
    
    public UUID getShipmentId() { return shipmentId; }
    public String getTrackingNumber() { return trackingNumber; }
    public UUID getOrderId() { return orderId; }
    public UUID getWarehouseId() { return warehouseId; }
}
