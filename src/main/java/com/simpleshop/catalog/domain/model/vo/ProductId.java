package com.simpleshop.catalog.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class ProductId extends ValueObject {
    
    private UUID value;
    
    protected ProductId() {}
    
    private ProductId(UUID value) {
        if (value == null) throw new IllegalArgumentException("ProductId cannot be null");
        this.value = value;
    }
    
    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
    
    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return Objects.equals(value, productId.value);
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
