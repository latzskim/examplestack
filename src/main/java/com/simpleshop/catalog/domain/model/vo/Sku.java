package com.simpleshop.catalog.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public final class Sku extends ValueObject {
    
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]+$");
    
    private String value;
    
    protected Sku() {}
    
    private Sku(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("SKU cannot be empty");
        if (value.length() > 50) throw new IllegalArgumentException("SKU cannot exceed 50 characters");
        if (!SKU_PATTERN.matcher(value).matches()) throw new IllegalArgumentException("SKU must be alphanumeric with dashes and underscores only");
        this.value = value.toUpperCase();
    }
    
    public static Sku of(String value) {
        return new Sku(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sku sku = (Sku) o;
        return Objects.equals(value, sku.value);
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
