package com.simpleshop.identity.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class UserId extends ValueObject {
    private UUID value;
    
    protected UserId() {}
    
    private UserId(UUID value) {
        if (value == null) throw new IllegalArgumentException("UserId cannot be null");
        this.value = value;
    }
    
    public static UserId of(UUID value) {
        return new UserId(value);
    }
    
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
    
    public static UserId fromString(String value) {
        return new UserId(UUID.fromString(value));
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
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
