package com.simpleshop.shared.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class Quantity extends ValueObject {
    private int value;
    
    protected Quantity() {}
    
    private Quantity(int value) {
        if (value < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.value = value;
    }
    
    public static Quantity of(int value) {
        return new Quantity(value);
    }
    
    public static Quantity zero() {
        return new Quantity(0);
    }
    
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }
    
    public Quantity subtract(Quantity other) {
        if (this.value < other.value) throw new IllegalArgumentException("Cannot subtract: result would be negative");
        return new Quantity(this.value - other.value);
    }
    
    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }
    
    public boolean isGreaterThanOrEqualTo(Quantity other) {
        return this.value >= other.value;
    }
    
    public boolean isZero() {
        return this.value == 0;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
