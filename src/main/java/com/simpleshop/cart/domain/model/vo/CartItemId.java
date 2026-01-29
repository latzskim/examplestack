package com.simpleshop.cart.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class CartItemId extends ValueObject {
    private UUID value;
    
    protected CartItemId() {}
    
    private CartItemId(UUID value) {
        if (value == null) throw new IllegalArgumentException("CartItemId cannot be null");
        this.value = value;
    }
    
    public static CartItemId of(UUID value) {
        return new CartItemId(value);
    }
    
    public static CartItemId generate() {
        return new CartItemId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemId that = (CartItemId) o;
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
