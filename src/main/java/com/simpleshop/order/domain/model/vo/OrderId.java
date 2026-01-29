package com.simpleshop.order.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class OrderId extends ValueObject {
    private UUID value;
    
    protected OrderId() {}
    
    private OrderId(UUID value) {
        if (value == null) throw new IllegalArgumentException("OrderId cannot be null");
        this.value = value;
    }
    
    public static OrderId of(UUID value) {
        return new OrderId(value);
    }
    
    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(value, orderId.value);
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
