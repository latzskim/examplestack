package com.simpleshop.inventory.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class WarehouseName extends ValueObject {
    
    private String value;
    
    protected WarehouseName() {}
    
    private WarehouseName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Warehouse name cannot be blank");
        if (value.length() > 100) throw new IllegalArgumentException("Warehouse name cannot exceed 100 characters");
        this.value = value;
    }
    
    public static WarehouseName of(String value) {
        return new WarehouseName(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarehouseName that = (WarehouseName) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
