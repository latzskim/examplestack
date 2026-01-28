package com.simpleshop.inventory.domain.event;

import com.simpleshop.inventory.domain.model.vo.StockId;
import com.simpleshop.shared.domain.model.DomainEvent;
import com.simpleshop.shared.domain.model.vo.Quantity;
import java.util.UUID;

public class StockReserved extends DomainEvent {
    
    private final StockId stockId;
    private final UUID productId;
    private final UUID warehouseId;
    private final Quantity amount;
    private final Quantity remainingAvailable;
    
    public StockReserved(StockId stockId, UUID productId, UUID warehouseId, Quantity amount, Quantity remainingAvailable) {
        super();
        this.stockId = stockId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.amount = amount;
        this.remainingAvailable = remainingAvailable;
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
    
    public Quantity getRemainingAvailable() {
        return remainingAvailable;
    }
}
