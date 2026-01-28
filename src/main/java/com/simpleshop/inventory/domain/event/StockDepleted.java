package com.simpleshop.inventory.domain.event;

import com.simpleshop.inventory.domain.model.vo.StockId;
import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class StockDepleted extends DomainEvent {
    
    private final StockId stockId;
    private final UUID productId;
    private final UUID warehouseId;
    
    public StockDepleted(StockId stockId, UUID productId, UUID warehouseId) {
        super();
        this.stockId = stockId;
        this.productId = productId;
        this.warehouseId = warehouseId;
    }
    
    public StockId getStockId() {
        return stockId;
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public UUID getWarehouseId() {
        return warehouseId;
    }
}
