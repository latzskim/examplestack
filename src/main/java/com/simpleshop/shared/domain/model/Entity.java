package com.simpleshop.shared.domain.model;

import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public abstract class Entity<ID> {
    public abstract ID getId();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return getId() != null && Objects.equals(getId(), entity.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
