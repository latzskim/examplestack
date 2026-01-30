package com.simpleshop.shipping.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class ShipmentNotFoundException extends DomainException {
    
    public ShipmentNotFoundException(UUID shipmentId) {
        super("Shipment not found: " + shipmentId);
    }
    
    public ShipmentNotFoundException(String trackingNumber) {
        super("Shipment not found with tracking number: " + trackingNumber);
    }
}
