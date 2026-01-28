package com.simpleshop.inventory.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import java.util.UUID;

public class StockNotFoundException extends DomainException {
    
    public StockNotFoundException(UUID productId, UUID warehouseId) {
        super("Stock not found for product " + productId + " in warehouse " + warehouseId);
    }
}
