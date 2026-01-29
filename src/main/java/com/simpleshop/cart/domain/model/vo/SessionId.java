package com.simpleshop.cart.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class SessionId extends ValueObject {
    private String value;
    
    protected SessionId() {}
    
    private SessionId(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("SessionId cannot be empty");
        this.value = value;
    }
    
    public static SessionId of(String value) {
        return new SessionId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
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
