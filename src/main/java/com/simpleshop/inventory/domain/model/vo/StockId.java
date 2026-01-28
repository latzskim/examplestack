package com.simpleshop.inventory.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class StockId extends ValueObject {
    
    private UUID value;
    
    protected StockId() {}
    
    private StockId(UUID value) {
        if (value == null) throw new IllegalArgumentException("StockId cannot be null");
        this.value = value;
    }
    
    public static StockId of(UUID value) {
        return new StockId(value);
    }
    
    public static StockId generate() {
        return new StockId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockId stockId = (StockId) o;
        return Objects.equals(value, stockId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}
