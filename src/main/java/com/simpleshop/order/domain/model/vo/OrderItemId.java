package com.simpleshop.order.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class OrderItemId extends ValueObject {
    private UUID value;
    
    protected OrderItemId() {}
    
    private OrderItemId(UUID value) {
        if (value == null) throw new IllegalArgumentException("OrderItemId cannot be null");
        this.value = value;
    }
    
    public static OrderItemId of(UUID value) {
        return new OrderItemId(value);
    }
    
    public static OrderItemId generate() {
        return new OrderItemId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemId that = (OrderItemId) o;
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
