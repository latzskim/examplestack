package com.simpleshop.catalog.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class CategoryId extends ValueObject {
    
    private UUID value;
    
    protected CategoryId() {}
    
    private CategoryId(UUID value) {
        if (value == null) throw new IllegalArgumentException("CategoryId cannot be null");
        this.value = value;
    }
    
    public static CategoryId of(UUID value) {
        return new CategoryId(value);
    }
    
    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
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
