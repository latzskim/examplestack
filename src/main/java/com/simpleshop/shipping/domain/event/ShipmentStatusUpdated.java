package com.simpleshop.shipping.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import java.util.UUID;

public class ShipmentStatusUpdated extends DomainEvent {
    private final UUID shipmentId;
    private final String trackingNumber;
    private final ShipmentStatus previousStatus;
    private final ShipmentStatus newStatus;
    private final String location;
    private final String notes;
    
    public ShipmentStatusUpdated(UUID shipmentId, String trackingNumber, 
                                  ShipmentStatus previousStatus, ShipmentStatus newStatus,
                                  String location, String notes) {
        super();
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.location = location;
        this.notes = notes;
    }
    
    public UUID getShipmentId() { return shipmentId; }
    public String getTrackingNumber() { return trackingNumber; }
    public ShipmentStatus getPreviousStatus() { return previousStatus; }
    public ShipmentStatus getNewStatus() { return newStatus; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }
}
