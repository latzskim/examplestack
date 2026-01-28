package com.simpleshop.inventory.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class WarehouseId extends ValueObject {
    
    private UUID value;
    
    protected WarehouseId() {}
    
    private WarehouseId(UUID value) {
        if (value == null) throw new IllegalArgumentException("WarehouseId cannot be null");
        this.value = value;
    }
    
    public static WarehouseId of(UUID value) {
        return new WarehouseId(value);
    }
    
    public static WarehouseId generate() {
        return new WarehouseId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarehouseId that = (WarehouseId) o;
        return Objects.equals(value, that.value);
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
