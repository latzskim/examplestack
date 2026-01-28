package com.simpleshop.inventory.domain.event;

import com.simpleshop.inventory.domain.model.vo.WarehouseId;
import com.simpleshop.shared.domain.model.DomainEvent;

public class WarehouseCreated extends DomainEvent {
    
    private final WarehouseId warehouseId;
    
    public WarehouseCreated(WarehouseId warehouseId) {
        super();
        this.warehouseId = warehouseId;
    }
    
    public WarehouseId getWarehouseId() {
        return warehouseId;
    }
}
