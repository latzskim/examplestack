package com.simpleshop.shipping.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class ShipmentDelivered extends DomainEvent {
    private final UUID shipmentId;
    private final String trackingNumber;
    private final UUID orderId;
    
    public ShipmentDelivered(UUID shipmentId, String trackingNumber, UUID orderId) {
        super();
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.orderId = orderId;
    }
    
    public UUID getShipmentId() { return shipmentId; }
    public String getTrackingNumber() { return trackingNumber; }
    public UUID getOrderId() { return orderId; }
}
