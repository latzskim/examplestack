package com.simpleshop.shipping.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class TrackingNumber extends ValueObject {
    private String value;
    
    protected TrackingNumber() {}
    
    private TrackingNumber(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Tracking number cannot be empty");
        if (value.length() > 50) throw new IllegalArgumentException("Tracking number cannot exceed 50 characters");
        this.value = value;
    }
    
    public static TrackingNumber of(String value) {
        return new TrackingNumber(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingNumber that = (TrackingNumber) o;
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
