package com.simpleshop.inventory.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class WarehouseNotFoundException extends DomainException {
    
    public WarehouseNotFoundException(UUID warehouseId) {
        super("Warehouse not found: " + warehouseId);
    }
}
