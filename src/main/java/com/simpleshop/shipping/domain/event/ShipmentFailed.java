package com.simpleshop.shipping.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class ShipmentFailed extends DomainEvent {
    private final UUID shipmentId;
    private final String trackingNumber;
    private final UUID orderId;
    private final String reason;
    
    public ShipmentFailed(UUID shipmentId, String trackingNumber, UUID orderId, String reason) {
        super();
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.orderId = orderId;
        this.reason = reason;
    }
    
    public UUID getShipmentId() { return shipmentId; }
    public String getTrackingNumber() { return trackingNumber; }
    public UUID getOrderId() { return orderId; }
    public String getReason() { return reason; }
}
