package com.simpleshop.cart.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class CartId extends ValueObject {
    private UUID value;
    
    protected CartId() {}
    
    private CartId(UUID value) {
        if (value == null) throw new IllegalArgumentException("CartId cannot be null");
        this.value = value;
    }
    
    public static CartId of(UUID value) {
        return new CartId(value);
    }
    
    public static CartId generate() {
        return new CartId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartId cartId = (CartId) o;
        return Objects.equals(value, cartId.value);
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
