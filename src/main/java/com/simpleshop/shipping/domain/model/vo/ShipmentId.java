package com.simpleshop.shipping.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class ShipmentId extends ValueObject {
    private UUID value;
    
    protected ShipmentId() {}
    
    private ShipmentId(UUID value) {
        if (value == null) throw new IllegalArgumentException("ShipmentId cannot be null");
        this.value = value;
    }
    
    public static ShipmentId of(UUID value) {
        return new ShipmentId(value);
    }
    
    public static ShipmentId generate() {
        return new ShipmentId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipmentId that = (ShipmentId) o;
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
