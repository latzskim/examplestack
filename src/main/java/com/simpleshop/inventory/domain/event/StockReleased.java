package com.simpleshop.inventory.domain.event;

import com.simpleshop.inventory.domain.model.vo.StockId;
import com.simpleshop.shared.domain.model.DomainEvent;
import com.simpleshop.shared.domain.model.vo.Quantity;
import java.util.UUID;

public class StockReleased extends DomainEvent {
    
    private final StockId stockId;
    private final UUID productId;
    private final UUID warehouseId;
    private final Quantity amount;
    
    public StockReleased(StockId stockId, UUID productId, UUID warehouseId, Quantity amount) {
        super();
        this.stockId = stockId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.amount = amount;
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
    
    public Quantity getAmount() {
        return amount;
    }
}
