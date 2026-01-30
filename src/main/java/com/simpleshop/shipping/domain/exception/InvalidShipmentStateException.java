package com.simpleshop.shipping.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;

public class InvalidShipmentStateException extends DomainException {
    
    public InvalidShipmentStateException(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        super(String.format("Cannot transition from %s to %s", currentStatus, newStatus));
    }
}
