package com.simpleshop.inventory.domain.model;

import com.simpleshop.inventory.domain.event.StockDepleted;
import com.simpleshop.inventory.domain.event.StockReleased;
import com.simpleshop.inventory.domain.event.StockReplenished;
import com.simpleshop.inventory.domain.event.StockReserved;
import com.simpleshop.inventory.domain.exception.InsufficientStockException;
import com.simpleshop.inventory.domain.model.vo.StockId;
import com.simpleshop.shared.domain.model.AggregateRoot;
import com.simpleshop.shared.domain.model.vo.Quantity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stocks")
public class Stock extends AggregateRoot<Stock> {
    
    @Id
    private UUID id;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private Quantity quantity;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "reserved_quantity", nullable = false))
    private Quantity reservedQuantity;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    protected Stock() {}
    
    private Stock(StockId id, UUID productId, UUID warehouseId, Quantity initialQuantity) {
        this.id = id.getValue();
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = initialQuantity;
        this.reservedQuantity = Quantity.zero();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public static Stock create(UUID productId, UUID warehouseId, Quantity initialQuantity) {
        if (productId == null) throw new IllegalArgumentException("Product ID cannot be null");
        if (warehouseId == null) throw new IllegalArgumentException("Warehouse ID cannot be null");
        if (initialQuantity == null) throw new IllegalArgumentException("Initial quantity cannot be null");
        
        return new Stock(StockId.generate(), productId, warehouseId, initialQuantity);
    }
    
    public void replenish(Quantity amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        
        this.quantity = this.quantity.add(amount);
        this.updatedAt = Instant.now();
        registerEvent(new StockReplenished(getStockId(), productId, warehouseId, amount, this.quantity));
    }
    
    public void reserve(Quantity amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        
        Quantity available = getAvailableQuantity();
        if (!available.isGreaterThanOrEqualTo(amount)) {
            throw new InsufficientStockException(productId, available.getValue(), amount.getValue());
        }
        
        this.reservedQuantity = this.reservedQuantity.add(amount);
        this.updatedAt = Instant.now();
        
        Quantity remainingAvailable = getAvailableQuantity();
        registerEvent(new StockReserved(getStockId(), productId, warehouseId, amount, remainingAvailable));
        
        if (remainingAvailable.isZero()) {
            registerEvent(new StockDepleted(getStockId(), productId, warehouseId));
        }
    }
    
    public void release(Quantity amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        
        this.reservedQuantity = this.reservedQuantity.subtract(amount);
        this.updatedAt = Instant.now();
        registerEvent(new StockReleased(getStockId(), productId, warehouseId, amount));
    }
    
    public void confirmReservation(Quantity amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        
        this.quantity = this.quantity.subtract(amount);
        this.reservedQuantity = this.reservedQuantity.subtract(amount);
        this.updatedAt = Instant.now();
    }
    
    public Quantity getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }
    
    public StockId getStockId() {
        return StockId.of(id);
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public UUID getWarehouseId() {
        return warehouseId;
    }
    
    public Quantity getQuantity() {
        return quantity;
    }
    
    public Quantity getReservedQuantity() {
        return reservedQuantity;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
