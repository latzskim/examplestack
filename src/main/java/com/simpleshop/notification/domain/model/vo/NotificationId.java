package com.simpleshop.notification.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class NotificationId extends ValueObject {
    
    private UUID value;
    
    protected NotificationId() {}
    
    private NotificationId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Notification ID cannot be null");
        }
        this.value = value;
    }
    
    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }
    
    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationId that = (NotificationId) o;
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
