package com.simpleshop.order.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Embeddable
public final class OrderNumber extends ValueObject {
    // TODO: It should be taken from DATABASE, on application restart, the sequence is being zeroed...
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    
    private String value;
    
    protected OrderNumber() {}
    
    private OrderNumber(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Order number cannot be empty");
        if (!value.matches("ORD-\\d{4}-\\d{5}")) throw new IllegalArgumentException("Invalid order number format");
        this.value = value;
    }
    
    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }
    
    public static OrderNumber generate() {
        int year = LocalDate.now().getYear();
        long seq = SEQUENCE.incrementAndGet();
        return new OrderNumber(String.format("ORD-%d-%05d", year, seq));
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderNumber that = (OrderNumber) o;
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
